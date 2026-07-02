const moneyDonationService = require('../services/moneyDonation.service');
const paymentService = require('../services/payment.service');
const receiptService = require('../services/receipt.service');
const emailService = require('../services/email.service');
const paymentConfig = require('../config/payment.config');

class MoneyDonationController {
  // Initiate donation - Create donation record and payment order
  async initiateDonation(req, res) {
    try {
      const { amount, donorName, donorEmail, donorPhone, message, isAnonymous, anonymousDonorName, paymentMethod } = req.body;

      // Validation
      if (!amount || !donorName || !donorEmail) {
        return res.status(400).json({
          success: false,
          message: 'Amount, donor name, and email are required'
        });
      }

      if (!['RAZORPAY', 'STRIPE', 'PAYPAL'].includes(paymentMethod || 'RAZORPAY')) {
        return res.status(400).json({
          success: false,
          message: 'Invalid payment method'
        });
      }

      // Create donation record
      const donation = await moneyDonationService.createDonation(
        {
          amount,
          donorName,
          donorEmail,
          donorPhone,
          message,
          isAnonymous: isAnonymous || false,
          anonymousDonorName,
          paymentMethod: paymentMethod || 'RAZORPAY'
        },
        req.user?.id || null,
        req.ip
      );

      // Create payment order (Razorpay)
      const order = await paymentService.createRazorpayOrder(
        amount,
        donorEmail,
        donorName,
        donation.id
      );

      return res.json({
        success: true,
        message: 'Donation initiated successfully',
        data: {
          donationId: donation.id,
          receiptNumber: donation.receiptNumber,
          transactionId: donation.transactionId,
          order: order,
          razorpayKeyId: paymentConfig.razorpayKeyId
        }
      });
    } catch (error) {
      console.error('Initiate donation error:', error);
      return res.status(400).json({
        success: false,
        message: error.message || 'Failed to initiate donation'
      });
    }
  }

  // Verify payment and update donation
  async verifyPayment(req, res) {
    try {
      const { donationId, orderId, paymentId, signature } = req.body;

      if (!donationId || !orderId || !paymentId || !signature) {
        return res.status(400).json({
          success: false,
          message: 'Missing payment verification data'
        });
      }

      // Verify signature
      await paymentService.verifyPaymentSignature(orderId, paymentId, signature);

      // Fetch payment details from Razorpay
      const paymentDetails = await paymentService.getPaymentDetails(paymentId);

      // Update donation with payment status
      let donation = await moneyDonationService.updatePaymentStatus(
        donationId,
        paymentDetails.status === 'captured' ? 'SUCCESS' : 'FAILED',
        {
          paymentId: paymentId,
          paymentSignature: signature,
          metadata: paymentDetails
        }
      );

      // If payment successful, generate receipt
      if (donation.paymentStatus === 'SUCCESS') {
        const receipt = await receiptService.generateReceiptPDF(donation);
        donation = await moneyDonationService.markReceiptGenerated(donationId, receipt.receiptUrl);

        // Send receipt email
        try {
          await emailService.sendEmail({
            to: donation.donorEmail,
            subject: `Your CareShare Donation Receipt #${donation.receiptNumber}`,
            html: receiptService._getReceiptEmailTemplate(donation)
          });
        } catch (emailError) {
          console.error('Failed to send receipt email:', emailError);
        }
      }

      return res.json({
        success: true,
        message: 'Payment verified successfully',
        data: {
          donationId: donation.id,
          paymentStatus: donation.paymentStatus,
          receiptNumber: donation.receiptNumber,
          receiptUrl: donation.receiptDownloadUrl
        }
      });
    } catch (error) {
      console.error('Verify payment error:', error);
      return res.status(400).json({
        success: false,
        message: error.message || 'Payment verification failed'
      });
    }
  }

  // Get donation details
  async getDonationDetails(req, res) {
    try {
      const { donationId } = req.params;

      const donation = await moneyDonationService.getDonationById(donationId);

      // Check if user can view this donation
      if (donation.userId && donation.userId !== req.user?.id && !req.user?.isAdmin) {
        return res.status(403).json({
          success: false,
          message: 'Unauthorized to view this donation'
        });
      }

      return res.json({
        success: true,
        data: donation
      });
    } catch (error) {
      console.error('Get donation details error:', error);
      return res.status(400).json({
        success: false,
        message: error.message || 'Failed to fetch donation details'
      });
    }
  }

  // Download receipt
  async downloadReceipt(req, res) {
    try {
      const { donationId } = req.params;

      const donation = await moneyDonationService.getDonationById(donationId);

      // Check authorization
      if (donation.userId && donation.userId !== req.user?.id && !req.user?.isAdmin) {
        return res.status(403).json({
          success: false,
          message: 'Unauthorized to download this receipt'
        });
      }

      if (!donation.receiptDownloadUrl) {
        return res.status(404).json({
          success: false,
          message: 'Receipt not available'
        });
      }

      // Extract filename from URL
      const fileName = donation.receiptDownloadUrl.split('/').pop();
      const receipt = await receiptService.getReceipt(fileName);

      // Send file for download
      res.download(receipt.filePath, `receipt_${donation.receiptNumber}.pdf`);
    } catch (error) {
      console.error('Download receipt error:', error);
      return res.status(400).json({
        success: false,
        message: error.message || 'Failed to download receipt'
      });
    }
  }

  // Get user's donations
  async getUserDonations(req, res) {
    try {
      const donations = await moneyDonationService.getUserDonations(req.user.id, req.user.email);

      return res.json({
        success: true,
        count: donations.length,
        data: donations
      });
    } catch (error) {
      console.error('Get user donations error:', error);
      return res.status(400).json({
        success: false,
        message: error.message || 'Failed to fetch user donations'
      });
    }
  }

  // Webhook for Razorpay (real-time payment updates)
  async handleRazorpayWebhook(req, res) {
    try {
      const signature = req.headers['x-razorpay-signature'];
      const body = req.body;

      // Verify webhook signature
      const crypto = require('crypto');
      const hmac = crypto.createHmac('sha256', paymentConfig.razorpayWebhookSecret);
      hmac.update(JSON.stringify(body));
      const generatedSignature = hmac.digest('hex');

      if (generatedSignature !== signature) {
        return res.status(401).json({ success: false, message: 'Invalid webhook signature' });
      }

      const event = body.event;
      const data = body.payload.payment.entity;

      if (event === 'payment.authorized' || event === 'payment.captured') {
        const donationId = data.notes.donationId;
        
        // Update donation status
        const donation = await moneyDonationService.updatePaymentStatus(
          donationId,
          'SUCCESS',
          {
            paymentId: data.id,
            paymentSignature: signature,
            metadata: data
          }
        );

        // Generate receipt if not already generated
        if (!donation.receiptDownloadUrl) {
          const receipt = await receiptService.generateReceiptPDF(donation);
          await moneyDonationService.markReceiptGenerated(donationId, receipt.receiptUrl);
        }
      }

      return res.json({ success: true });
    } catch (error) {
      console.error('Webhook error:', error);
      return res.status(400).json({
        success: false,
        message: 'Webhook processing failed'
      });
    }
  }
}

module.exports = new MoneyDonationController();
