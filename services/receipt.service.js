const fs = require('fs-extra');
const path = require('path');
const receiptGenerator = require('../utils/receipt-generator');

class ReceiptService {
  constructor() {
    this.uploadsDir = path.join(__dirname, '../uploads/receipts');
  }

  async initializeReceiptDirectory() {
    try {
      await fs.ensureDir(this.uploadsDir);
    } catch (error) {
      throw new Error(`Failed to initialize receipt directory: ${error.message}`);
    }
  }

  // Generate and save receipt
  async generateReceiptPDF(donationData) {
    try {
      await this.initializeReceiptDirectory();

      const fileName = `receipt_${donationData.receiptNumber}_${Date.now()}.pdf`;
      const outputPath = path.join(this.uploadsDir, fileName);

      // Generate PDF
      await receiptGenerator.generateReceipt(donationData, outputPath);

      // Return relative URL
      const receiptUrl = `/uploads/receipts/${fileName}`;

      return {
        fileName: fileName,
        filePath: outputPath,
        receiptUrl: receiptUrl
      };
    } catch (error) {
      throw new Error(`Failed to generate receipt: ${error.message}`);
    }
  }

  // Download receipt
  async getReceipt(fileName) {
    try {
      const filePath = path.join(this.uploadsDir, fileName);

      const fileExists = await fs.pathExists(filePath);
      if (!fileExists) {
        throw new Error('Receipt file not found');
      }

      return {
        filePath: filePath,
        fileName: fileName
      };
    } catch (error) {
      throw new Error(`Failed to retrieve receipt: ${error.message}`);
    }
  }

  // Delete receipt
  async deleteReceipt(fileName) {
    try {
      const filePath = path.join(this.uploadsDir, fileName);
      await fs.remove(filePath);
    } catch (error) {
      throw new Error(`Failed to delete receipt: ${error.message}`);
    }
  }

  // Resend receipt email
  async resendReceiptEmail(donation, emailService) {
    try {
      const emailContent = {
        to: donation.donorEmail,
        subject: `Your CareShare Donation Receipt #${donation.receiptNumber}`,
        html: this._getReceiptEmailTemplate(donation)
      };

      await emailService.sendEmail(emailContent);
    } catch (error) {
      throw new Error(`Failed to resend receipt email: ${error.message}`);
    }
  }

  _getReceiptEmailTemplate(donation) {
    return `
      <h2>Thank You for Your Donation!</h2>
      <p>Dear ${donation.isAnonymous ? donation.anonymousDonorName || 'Donor' : donation.donorName},</p>
      <p>We're grateful for your generous donation of <strong>₹${parseFloat(donation.amount).toFixed(2)}</strong> to CareShare.</p>
      <div style="background-color: #f5f5f5; padding: 15px; margin: 20px 0; border-radius: 5px;">
        <p><strong>Receipt Number:</strong> ${donation.receiptNumber}</p>
        <p><strong>Transaction ID:</strong> ${donation.transactionId}</p>
        <p><strong>Payment Status:</strong> ${donation.paymentStatus}</p>
        <p><strong>Date:</strong> ${new Date(donation.createdAt).toLocaleDateString('en-IN')}</p>
      </div>
      <p>Your receipt is attached to this email. You can also download it anytime from your dashboard.</p>
      <p>Thank you for making a difference!</p>
      <p>Best regards,<br>CareShare Team</p>
    `;
  }
}

module.exports = new ReceiptService();
