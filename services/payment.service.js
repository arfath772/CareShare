const paymentConfig = require('../config/payment.config');
const paymentUtils = require('../utils/payment-utils');

class PaymentService {
  // Create Razorpay order
  async createRazorpayOrder(amount, donorEmail, donorName, donationId) {
    try {
      // Validate amount in paise (₹1 = 100 paise)
      const amountInPaise = Math.round(amount * 100);

      if (amountInPaise < 100) {
        throw new Error('Minimum donation amount is ₹1');
      }

      if (amountInPaise > paymentConfig.maxAmount * 100) {
        throw new Error(`Maximum donation amount is ₹${paymentConfig.maxAmount}`);
      }

      const orderOptions = {
        amount: amountInPaise,
        currency: paymentConfig.currency,
        receipt: `donation_${donationId}`,
        customer_notify: 1,
        notes: {
          donationId: donationId,
          donorEmail: donorEmail,
          donorName: donorName
        }
      };

      const order = await paymentConfig.razorpay.orders.create(orderOptions);

      return {
        orderId: order.id,
        amount: order.amount,
        currency: order.currency,
        status: order.status
      };
    } catch (error) {
      throw new Error(`Failed to create Razorpay order: ${error.message}`);
    }
  }

  // Verify payment signature
  async verifyPaymentSignature(orderId, paymentId, signature) {
    try {
      const isValid = paymentUtils.verifyRazorpaySignature(
        orderId,
        paymentId,
        signature,
        paymentConfig.razorpayWebhookSecret
      );

      if (!isValid) {
        throw new Error('Invalid payment signature');
      }

      return true;
    } catch (error) {
      throw new Error(`Payment verification failed: ${error.message}`);
    }
  }

  // Fetch payment details from Razorpay
  async getPaymentDetails(paymentId) {
    try {
      const payment = await paymentConfig.razorpay.payments.fetch(paymentId);
      return payment;
    } catch (error) {
      throw new Error(`Failed to fetch payment details: ${error.message}`);
    }
  }

  // Refund payment
  async refundPayment(paymentId, amount = null, reason = '') {
    try {
      const refundOptions = {
        notes: {
          reason: reason
        }
      };

      if (amount) {
        refundOptions.amount = Math.round(amount * 100);
      }

      const refund = await paymentConfig.razorpay.payments.refund(paymentId, refundOptions);

      return {
        refundId: refund.id,
        status: refund.status,
        amount: refund.amount / 100
      };
    } catch (error) {
      throw new Error(`Failed to refund payment: ${error.message}`);
    }
  }
}

module.exports = new PaymentService();
