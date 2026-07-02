# CareShare - Money Donation Feature Implementation Plan

## Executive Summary
This document provides a complete implementation roadmap for adding a cash/money donation feature to the CareShare platform. The feature will allow users to donate any amount starting from ₹1 with automatic receipt generation in PDF format and admin dashboard tracking.

---

## 1. DATABASE SCHEMA CHANGES

### 1.1 New Model: `moneyDonation.model.js`
Create a new MoneyDonation model to track all cash donations:

```javascript
const { DataTypes } = require('sequelize');
const sequelize = require('../config/db.config');

const MoneyDonation = sequelize.define('MoneyDonation', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  donorName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  donorEmail: {
    type: DataTypes.STRING,
    allowNull: false
  },
  donorPhone: {
    type: DataTypes.STRING,
    allowNull: true
  },
  amount: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false,
    validate: {
      min: 1.00
    }
  },
  currency: {
    type: DataTypes.ENUM('INR'),
    defaultValue: 'INR',
    allowNull: false
  },
  paymentMethod: {
    type: DataTypes.ENUM('RAZORPAY', 'STRIPE', 'PAYPAL'),
    allowNull: false
  },
  transactionId: {
    type: DataTypes.STRING,
    unique: true,
    allowNull: false
  },
  paymentId: {
    type: DataTypes.STRING,
    allowNull: true
  },
  paymentSignature: {
    type: DataTypes.STRING,
    allowNull: true
  },
  paymentStatus: {
    type: DataTypes.ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'),
    defaultValue: 'PENDING',
    allowNull: false
  },
  receiptNumber: {
    type: DataTypes.STRING,
    unique: true,
    allowNull: false
  },
  receiptDownloadUrl: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  message: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  isAnonymous: {
    type: DataTypes.BOOLEAN,
    defaultValue: false,
    allowNull: false
  },
  anonymousDonorName: {
    type: DataTypes.STRING,
    allowNull: true
  },
  userId: {
    type: DataTypes.BIGINT,
    allowNull: true,
    references: {
      model: 'Users',
      key: 'id'
    }
  },
  receiptGeneratedAt: {
    type: DataTypes.DATE,
    allowNull: true
  },
  receiptExpiry: {
    type: DataTypes.DATE,
    allowNull: true
  },
  ipAddress: {
    type: DataTypes.STRING,
    allowNull: true
  },
  metadata: {
    type: DataTypes.JSON,
    allowNull: true
  }
}, {
  timestamps: true,
  tableName: 'MoneyDonations'
});

module.exports = MoneyDonation;
```

### 1.2 Update `models/index.js`
Add the MoneyDonation model and relationships:

```javascript
const MoneyDonation = require('./moneyDonation.model');

// ... existing relationships ...

// User - MoneyDonation relationship
User.hasMany(MoneyDonation, { foreignKey: 'userId', as: 'moneyDonations' });
MoneyDonation.belongsTo(User, { foreignKey: 'userId', as: 'donor' });
```

---

## 2. PAYMENT GATEWAY INTEGRATION

### 2.1 Payment Gateway Selection: RAZORPAY (Recommended for India)
**Why Razorpay?**
- Best for INR transactions
- Simple integration
- Good documentation
- Supports multiple payment methods
- PCI-DSS compliant
- Refund support built-in

### 2.2 Installation
Add Razorpay to `package.json`:
```bash
npm install razorpay
```

### 2.3 Environment Variables
Add to `.env`:
```
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# Company Details for Receipt
COMPANY_NAME=CareShare
COMPANY_ADDRESS=Your Company Address, City, State, PIN
COMPANY_EMAIL=info@careshare.com
COMPANY_PHONE=+91-XXXXXXXXXX
COMPANY_REGISTRATION_NUMBER=ABC123456
COMPANY_TAX_ID=GSTIN123456

# PDF Receipt Settings
RECEIPT_LOGO_PATH=./assets/images/logo.png
RECEIPT_TEMPLATE_PATH=./templates/receipt-template.html
PDF_GENERATION_TIMEOUT=30000
```

---

## 3. BACKEND IMPLEMENTATION

### 3.1 File Structure
```
CareShare/
├── models/
│   └── moneyDonation.model.js (NEW)
├── services/
│   ├── moneyDonation.service.js (NEW)
│   ├── payment.service.js (NEW)
│   └── receipt.service.js (NEW)
├── controllers/
│   ├── moneyDonation.controller.js (NEW)
│   └── admin.controller.js (UPDATE)
├── routes/
│   ├── moneyDonation.routes.js (NEW)
│   └── admin.routes.js (UPDATE)
├── middleware/
│   └── payment.middleware.js (NEW)
├── config/
│   └── payment.config.js (NEW)
├── utils/
│   ├── receipt-generator.js (NEW)
│   └── payment-utils.js (NEW)
└── scripts/
    └── generate-receipt.js (NEW - utility script)
```

### 3.2 Config: `config/payment.config.js` (NEW)
```javascript
const Razorpay = require('razorpay');
require('dotenv').config();

const razorpayInstance = new Razorpay({
  key_id: process.env.RAZORPAY_KEY_ID,
  key_secret: process.env.RAZORPAY_KEY_SECRET
});

module.exports = {
  razorpay: razorpayInstance,
  razorpayKeyId: process.env.RAZORPAY_KEY_ID,
  razorpayWebhookSecret: process.env.RAZORPAY_WEBHOOK_SECRET,
  minAmount: 1,
  maxAmount: 100000,
  currency: 'INR'
};
```

### 3.3 Utils: `utils/receipt-generator.js` (NEW)
Generates PDF receipts using PDFKit:
```javascript
const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');
require('dotenv').config();

class ReceiptGenerator {
  constructor() {
    this.fontSize = {
      header: 20,
      subheader: 14,
      normal: 10,
      small: 8
    };
    this.colors = {
      primary: '#1e3c72',
      secondary: '#2a5298',
      accent: '#f39c12',
      text: '#333333',
      lightGray: '#f5f5f5'
    };
  }

  async generateReceipt(donationData, outputPath) {
    return new Promise((resolve, reject) => {
      try {
        const doc = new PDFDocument({
          size: 'A4',
          margin: 40
        });

        const writeStream = fs.createWriteStream(outputPath);
        doc.pipe(writeStream);

        // Header
        this._addHeader(doc, donationData);
        
        // Donation Details
        doc.moveDown(1);
        this._addDonationDetails(doc, donationData);
        
        // Company Details
        doc.moveDown(2);
        this._addCompanyDetails(doc);
        
        // Payment Info
        doc.moveDown(1);
        this._addPaymentInfo(doc, donationData);
        
        // Thank You Message
        doc.moveDown(2);
        this._addThankYouMessage(doc);
        
        // Footer
        doc.moveDown(3);
        this._addFooter(doc);

        doc.end();

        writeStream.on('finish', () => {
          resolve(outputPath);
        });

        writeStream.on('error', (err) => {
          reject(err);
        });
      } catch (error) {
        reject(error);
      }
    });
  }

  _addHeader(doc, donationData) {
    const pageWidth = doc.page.width;
    const margin = doc.page.margins.left;

    // Receipt Title
    doc.fontSize(this.fontSize.header)
       .fillColor(this.colors.primary)
       .font('Helvetica-Bold')
       .text('DONATION RECEIPT', { align: 'center' });

    doc.moveDown(0.5);
    doc.fontSize(this.fontSize.small)
       .fillColor(this.colors.secondary)
       .text(`Receipt #${donationData.receiptNumber}`, { align: 'center' });

    doc.moveDown(0.3);
    const receiptDate = new Date(donationData.createdAt).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
    doc.fontSize(this.fontSize.small)
       .fillColor(this.colors.text)
       .text(`Date: ${receiptDate}`, { align: 'center' });

    // Horizontal line
    doc.moveDown(0.5);
    doc.strokeColor(this.colors.secondary)
       .lineWidth(2)
       .lineTo(pageWidth - margin, doc.y)
       .stroke();
  }

  _addDonationDetails(doc, donationData) {
    doc.fontSize(this.fontSize.normal)
       .fillColor(this.colors.text)
       .font('Helvetica-Bold')
       .text('Donation Details:');

    doc.moveDown(0.3);
    doc.fontSize(this.fontSize.normal)
       .font('Helvetica');

    const details = [
      { label: 'Donor Name:', value: donationData.isAnonymous ? donationData.anonymousDonorName || 'Anonymous Donor' : donationData.donorName },
      { label: 'Email:', value: donationData.isAnonymous ? '***@***.com' : donationData.donorEmail },
      { label: 'Donated Amount:', value: `₹${parseFloat(donationData.amount).toFixed(2)}` },
      { label: 'Currency:', value: donationData.currency },
      { label: 'Payment Method:', value: donationData.paymentMethod },
      { label: 'Transaction ID:', value: donationData.transactionId }
    ];

    const leftMargin = doc.page.margins.left;
    const labelWidth = 120;

    details.forEach(detail => {
      doc.fontSize(this.fontSize.normal);
      doc.text(detail.label, leftMargin, doc.y, { width: labelWidth })
         .fontSize(this.fontSize.normal)
         .font('Helvetica-Bold')
         .text(detail.value, leftMargin + labelWidth, doc.y - doc.currentLineHeight(this.fontSize.normal));
      doc.moveDown(0.6);
    });
  }

  _addCompanyDetails(doc) {
    doc.fontSize(this.fontSize.normal)
       .fillColor(this.colors.text)
       .font('Helvetica-Bold')
       .text('Donated To:');

    doc.moveDown(0.3);
    doc.fontSize(this.fontSize.normal)
       .font('Helvetica')
       .fillColor(this.colors.secondary);

    const companyDetails = [
      process.env.COMPANY_NAME || 'CareShare',
      process.env.COMPANY_ADDRESS || 'Company Address',
      `Email: ${process.env.COMPANY_EMAIL}`,
      `Phone: ${process.env.COMPANY_PHONE}`,
      `Registration: ${process.env.COMPANY_REGISTRATION_NUMBER}`,
      `Tax ID: ${process.env.COMPANY_TAX_ID}`
    ];

    companyDetails.forEach(detail => {
      doc.text(detail);
      doc.moveDown(0.3);
    });
  }

  _addPaymentInfo(doc, donationData) {
    // Horizontal line
    const pageWidth = doc.page.width;
    const margin = doc.page.margins.left;
    doc.strokeColor(this.colors.secondary)
       .lineWidth(1)
       .moveTo(margin, doc.y)
       .lineTo(pageWidth - margin, doc.y)
       .stroke();

    doc.moveDown(0.5);
    doc.fontSize(this.fontSize.normal)
       .fillColor(this.colors.text)
       .font('Helvetica-Bold')
       .text('Payment Information:');

    doc.moveDown(0.3);
    doc.fontSize(this.fontSize.normal)
       .font('Helvetica');

    const paymentStatus = donationData.paymentStatus.charAt(0) + donationData.paymentStatus.slice(1).toLowerCase();
    const statusColor = donationData.paymentStatus === 'SUCCESS' ? '#27ae60' : '#e74c3c';

    doc.fillColor(statusColor)
       .text(`Status: ${paymentStatus}`);

    doc.moveDown(0.3);
    doc.fillColor(this.colors.text)
       .text(`Payment ID: ${donationData.paymentId}`);

    doc.moveDown(0.3);
    doc.text(`Signature: ${donationData.paymentSignature}`);
  }

  _addThankYouMessage(doc) {
    doc.fontSize(this.fontSize.normal)
       .fillColor(this.colors.primary)
       .font('Helvetica-Bold')
       .text('Thank You for Your Generosity!', { align: 'center' });

    doc.moveDown(0.5);
    doc.fontSize(this.fontSize.small)
       .fillColor(this.colors.text)
       .font('Helvetica')
       .text('Your contribution will make a significant difference in helping those in need. We are grateful for your support and commitment to creating a more compassionate world.', { align: 'justify' });
  }

  _addFooter(doc) {
    const pageWidth = doc.page.width;
    const margin = doc.page.margins.left;

    // Horizontal line
    doc.moveDown(1);
    doc.strokeColor(this.colors.secondary)
       .lineWidth(1)
       .moveTo(margin, doc.y)
       .lineTo(pageWidth - margin, doc.y)
       .stroke();

    doc.moveDown(0.3);
    doc.fontSize(this.fontSize.small)
       .fillColor(this.colors.text)
       .text('This is an electronically generated receipt and does not require a signature.', { align: 'center' });

    doc.fontSize(this.fontSize.small)
       .text(`Generated on ${new Date().toLocaleDateString('en-IN')} at ${new Date().toLocaleTimeString('en-IN')}`, { align: 'center' });

    doc.fontSize(this.fontSize.small)
       .fillColor(this.colors.secondary)
       .text('For more information, visit: www.careshare.com', { align: 'center' });
  }
}

module.exports = new ReceiptGenerator();
```

### 3.4 Utils: `utils/payment-utils.js` (NEW)
```javascript
const { v4: uuidv4 } = require('uuid');
const crypto = require('crypto');

class PaymentUtils {
  // Generate unique receipt number
  generateReceiptNumber() {
    const date = new Date();
    const dateStr = date.getFullYear().toString() +
                    String(date.getMonth() + 1).padStart(2, '0') +
                    String(date.getDate()).padStart(2, '0');
    const randomStr = Math.random().toString(36).substring(2, 10).toUpperCase();
    return `RCP-${dateStr}-${randomStr}`;
  }

  // Generate transaction ID
  generateTransactionId() {
    return `TXN-${Date.now()}-${Math.random().toString(36).substring(2, 10).toUpperCase()}`;
  }

  // Verify Razorpay signature
  verifyRazorpaySignature(orderId, paymentId, signature, secret) {
    const hmac = crypto.createHmac('sha256', secret);
    hmac.update(orderId + '|' + paymentId);
    const generatedSignature = hmac.digest('hex');
    return generatedSignature === signature;
  }

  // Validate donation amount
  validateAmount(amount, minAmount = 1, maxAmount = 100000) {
    const numAmount = parseFloat(amount);
    if (isNaN(numAmount) || numAmount < minAmount || numAmount > maxAmount) {
      throw new Error(`Amount must be between ₹${minAmount} and ₹${maxAmount}`);
    }
    return numAmount;
  }

  // Sanitize donor data
  sanitizeDonorData(data) {
    return {
      donorName: (data.donorName || '').trim(),
      donorEmail: (data.donorEmail || '').trim().toLowerCase(),
      donorPhone: (data.donorPhone || '').trim() || null,
      message: (data.message || '').trim() || null,
      isAnonymous: Boolean(data.isAnonymous),
      anonymousDonorName: data.isAnonymous ? (data.anonymousDonorName || 'Anonymous Donor').trim() : null
    };
  }
}

module.exports = new PaymentUtils();
```

### 3.5 Service: `services/moneyDonation.service.js` (NEW)
```javascript
const { MoneyDonation, User } = require('../models');
const paymentUtils = require('../utils/payment-utils');

class MoneyDonationService {
  // Create a new money donation record
  async createDonation(donationData, userId = null, ipAddress = null) {
    try {
      const sanitizedData = paymentUtils.sanitizeDonorData(donationData);
      
      const donation = await MoneyDonation.create({
        donorName: sanitizedData.donorName,
        donorEmail: sanitizedData.donorEmail,
        donorPhone: sanitizedData.donorPhone,
        amount: paymentUtils.validateAmount(donationData.amount),
        currency: 'INR',
        paymentMethod: donationData.paymentMethod,
        transactionId: paymentUtils.generateTransactionId(),
        receiptNumber: paymentUtils.generateReceiptNumber(),
        message: sanitizedData.message,
        isAnonymous: sanitizedData.isAnonymous,
        anonymousDonorName: sanitizedData.anonymousDonorName,
        userId: userId,
        ipAddress: ipAddress,
        metadata: donationData.metadata || {}
      });

      return donation;
    } catch (error) {
      throw new Error(`Failed to create donation: ${error.message}`);
    }
  }

  // Get donation by ID
  async getDonationById(donationId) {
    try {
      const donation = await MoneyDonation.findByPk(donationId, {
        include: ['donor']
      });

      if (!donation) {
        throw new Error('Donation not found');
      }

      return donation;
    } catch (error) {
      throw new Error(`Failed to fetch donation: ${error.message}`);
    }
  }

  // Get all donations by user
  async getUserDonations(userId) {
    try {
      const donations = await MoneyDonation.findAll({
        where: { userId },
        order: [['createdAt', 'DESC']]
      });

      return donations;
    } catch (error) {
      throw new Error(`Failed to fetch user donations: ${error.message}`);
    }
  }

  // Update donation payment status
  async updatePaymentStatus(donationId, status, paymentData) {
    try {
      const donation = await MoneyDonation.findByPk(donationId);

      if (!donation) {
        throw new Error('Donation not found');
      }

      await donation.update({
        paymentStatus: status,
        paymentId: paymentData.paymentId,
        paymentSignature: paymentData.paymentSignature,
        metadata: paymentData.metadata || donation.metadata
      });

      return donation;
    } catch (error) {
      throw new Error(`Failed to update payment status: ${error.message}`);
    }
  }

  // Mark receipt as generated
  async markReceiptGenerated(donationId, receiptUrl) {
    try {
      const donation = await MoneyDonation.findByPk(donationId);

      if (!donation) {
        throw new Error('Donation not found');
      }

      await donation.update({
        receiptDownloadUrl: receiptUrl,
        receiptGeneratedAt: new Date()
      });

      return donation;
    } catch (error) {
      throw new Error(`Failed to mark receipt generated: ${error.message}`);
    }
  }

  // Get all donations (for admin)
  async getAllDonations(filters = {}) {
    try {
      const whereClause = {};

      if (filters.status) {
        whereClause.paymentStatus = filters.status;
      }

      if (filters.startDate || filters.endDate) {
        whereClause.createdAt = {};
        if (filters.startDate) {
          whereClause.createdAt.$gte = new Date(filters.startDate);
        }
        if (filters.endDate) {
          whereClause.createdAt.$lte = new Date(filters.endDate);
        }
      }

      const donations = await MoneyDonation.findAll({
        where: whereClause,
        include: ['donor'],
        order: [['createdAt', 'DESC']],
        limit: filters.limit || 100,
        offset: filters.offset || 0
      });

      return donations;
    } catch (error) {
      throw new Error(`Failed to fetch donations: ${error.message}`);
    }
  }

  // Get donation statistics (for admin dashboard)
  async getDonationStats() {
    try {
      const totalDonations = await MoneyDonation.count();
      const successfulDonations = await MoneyDonation.count({
        where: { paymentStatus: 'SUCCESS' }
      });
      const totalAmount = await MoneyDonation.sum('amount', {
        where: { paymentStatus: 'SUCCESS' }
      });
      const averageDonation = await MoneyDonation.findAll({
        attributes: [
          [sequelize.fn('AVG', sequelize.col('amount')), 'avgAmount']
        ],
        where: { paymentStatus: 'SUCCESS' },
        raw: true
      });

      return {
        totalDonations,
        successfulDonations,
        totalAmount: totalAmount || 0,
        averageDonation: averageDonation[0]?.avgAmount || 0,
        successRate: totalDonations > 0 ? ((successfulDonations / totalDonations) * 100).toFixed(2) : 0
      };
    } catch (error) {
      throw new Error(`Failed to fetch statistics: ${error.message}`);
    }
  }

  // Refund a donation
  async refundDonation(donationId, reason) {
    try {
      const donation = await MoneyDonation.findByPk(donationId);

      if (!donation) {
        throw new Error('Donation not found');
      }

      await donation.update({
        paymentStatus: 'REFUNDED',
        metadata: {
          ...donation.metadata,
          refundReason: reason,
          refundedAt: new Date().toISOString()
        }
      });

      return donation;
    } catch (error) {
      throw new Error(`Failed to refund donation: ${error.message}`);
    }
  }
}

module.exports = new MoneyDonationService();
```

### 3.6 Service: `services/payment.service.js` (NEW)
```javascript
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
        amount: amount ? Math.round(amount * 100) : undefined,
        notes: {
          reason: reason
        }
      };

      // Remove undefined fields
      Object.keys(refundOptions).forEach(key => 
        refundOptions[key] === undefined && delete refundOptions[key]
      );

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
```

### 3.7 Service: `services/receipt.service.js` (NEW)
```javascript
const fs = require('fs-extra');
const path = require('path');
const receiptGenerator = require('../utils/receipt-generator');

class ReceiptService {
  constructor() {
    this.uploadsDir = path.join(__dirname, '../uploads/receipts');
    this.fs = fs;
  }

  async initializeReceiptDirectory() {
    try {
      await this.fs.ensureDir(this.uploadsDir);
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

      const fileExists = await this.fs.pathExists(filePath);
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
      await this.fs.remove(filePath);
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
```

### 3.8 Controller: `controllers/moneyDonation.controller.js` (NEW)
```javascript
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
      const donation = await moneyDonationService.updatePaymentStatus(
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
        await moneyDonationService.markReceiptGenerated(donationId, receipt.receiptUrl);

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
      const donations = await moneyDonationService.getUserDonations(req.user.id);

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
```

### 3.9 Routes: `routes/moneyDonation.routes.js` (NEW)
```javascript
const express = require('express');
const router = express.Router();
const moneyDonationController = require('../controllers/moneyDonation.controller');
const { authenticateToken } = require('../middleware/auth.middleware');
const { isAdmin } = require('../middleware/admin.middleware');

// Public routes (no authentication required)
router.post('/initiate', moneyDonationController.initiateDonation);
router.post('/verify-payment', moneyDonationController.verifyPayment);
router.post('/webhook/razorpay', moneyDonationController.handleRazorpayWebhook);

// Authenticated routes
router.get('/details/:donationId', authenticateToken, moneyDonationController.getDonationDetails);
router.get('/download/:donationId', authenticateToken, moneyDonationController.downloadReceipt);
router.get('/my-donations', authenticateToken, moneyDonationController.getUserDonations);

module.exports = router;
```

### 3.10 Update Admin Controller
Add these methods to `controllers/admin.controller.js`:
```javascript
// Get all money donations
async getAllMoneyDonations(req, res) {
  try {
    const { status, startDate, endDate, limit, offset } = req.query;
    
    const donations = await moneyDonationService.getAllDonations({
      status,
      startDate,
      endDate,
      limit: parseInt(limit) || 100,
      offset: parseInt(offset) || 0
    });

    return res.json({
      success: true,
      data: donations
    });
  } catch (error) {
    console.error('Get money donations error:', error);
    return res.status(400).json({
      success: false,
      message: error.message
    });
  }
}

// Get money donation statistics
async getMoneyDonationStats(req, res) {
  try {
    const stats = await moneyDonationService.getDonationStats();
    return res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('Get stats error:', error);
    return res.status(400).json({
      success: false,
      message: error.message
    });
  }
}

// Refund money donation
async refundMoneyDonation(req, res) {
  try {
    const { donationId } = req.params;
    const { reason } = req.body;

    const donation = await moneyDonationService.getDonationById(donationId);
    
    if (donation.paymentStatus !== 'SUCCESS') {
      return res.status(400).json({
        success: false,
        message: 'Cannot refund a donation that is not successful'
      });
    }

    // Refund via payment gateway
    const refund = await paymentService.refundPayment(donation.paymentId, donation.amount, reason);
    
    // Update donation record
    await moneyDonationService.refundDonation(donationId, reason);

    return res.json({
      success: true,
      message: 'Donation refunded successfully',
      data: { refund }
    });
  } catch (error) {
    console.error('Refund error:', error);
    return res.status(400).json({
      success: false,
      message: error.message
    });
  }
}
```

### 3.11 Update Admin Routes
Add to `routes/admin.routes.js`:
```javascript
// Money donation management
router.get('/money-donations', adminController.getAllMoneyDonations);
router.get('/money-donations/stats', adminController.getMoneyDonationStats);
router.post('/money-donations/:donationId/refund', adminController.refundMoneyDonation);
```

### 3.12 Update `server.js`
Add the money donation routes:
```javascript
const moneyDonationRoutes = require('./routes/moneyDonation.routes');

// ... existing middleware ...

// Add to routes section
app.use('/api/donations/money', moneyDonationRoutes);
```

---

## 4. FRONTEND IMPLEMENTATION

### 4.1 Create Money Donation Page: `src/main/resources/templates/donate-money.html`
```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Donate Money - CareShare</title>
  <link rel="stylesheet" href="/css/styles.css">
  <style>
    .donation-container {
      max-width: 600px;
      margin: 40px auto;
      padding: 30px;
      background: white;
      border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    .amount-presets {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 10px;
      margin: 20px 0;
    }
    
    .amount-btn {
      padding: 12px;
      border: 2px solid #ddd;
      background: white;
      cursor: pointer;
      border-radius: 5px;
      font-weight: bold;
      transition: all 0.3s;
    }
    
    .amount-btn:hover,
    .amount-btn.active {
      border-color: #007bff;
      background: #007bff;
      color: white;
    }
    
    .form-group {
      margin: 15px 0;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 5px;
      font-weight: bold;
    }
    
    .form-group input,
    .form-group textarea {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-family: inherit;
    }
    
    .checkbox-group {
      display: flex;
      align-items: center;
      margin: 15px 0;
    }
    
    .checkbox-group input {
      width: auto;
      margin-right: 10px;
    }
    
    .amount-display {
      font-size: 28px;
      font-weight: bold;
      color: #007bff;
      text-align: center;
      margin: 20px 0;
    }
    
    .btn-donate {
      width: 100%;
      padding: 15px;
      background: #28a745;
      color: white;
      border: none;
      border-radius: 5px;
      font-size: 16px;
      font-weight: bold;
      cursor: pointer;
      transition: background 0.3s;
    }
    
    .btn-donate:hover {
      background: #218838;
    }
    
    .btn-donate:disabled {
      background: #ccc;
      cursor: not-allowed;
    }
    
    .error-message {
      color: #dc3545;
      margin-top: 10px;
      padding: 10px;
      background: #f8d7da;
      border-radius: 5px;
      display: none;
    }
    
    .success-message {
      color: #155724;
      margin-top: 10px;
      padding: 10px;
      background: #d4edda;
      border-radius: 5px;
      display: none;
    }
    
    .loading {
      display: none;
      text-align: center;
      padding: 20px;
    }
    
    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #007bff;
      border-radius: 50%;
      width: 30px;
      height: 30px;
      animation: spin 1s linear infinite;
      margin: 0 auto;
    }
    
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  </style>
</head>
<body>
  <div class="donation-container">
    <h1>💝 Make a Donation</h1>
    <p class="text-muted">Your generosity makes a difference. Donate any amount from ₹1 onwards.</p>
    
    <form id="donationForm">
      <div class="form-group">
        <label>Select or Enter Amount (₹)</label>
        <div class="amount-presets">
          <button type="button" class="amount-btn" data-amount="100">₹100</button>
          <button type="button" class="amount-btn" data-amount="500">₹500</button>
          <button type="button" class="amount-btn" data-amount="1000">₹1000</button>
          <button type="button" class="amount-btn" data-amount="2000">₹2000</button>
          <button type="button" class="amount-btn" data-amount="5000">₹5000</button>
          <button type="button" class="amount-btn" data-amount="custom">Custom</button>
        </div>
        <input type="number" id="amount" name="amount" placeholder="Enter amount in ₹" min="1" max="100000" required>
      </div>
      
      <div class="amount-display" id="amountDisplay">₹0</div>
      
      <div class="form-group">
        <label for="donorName">Your Name</label>
        <input type="text" id="donorName" name="donorName" placeholder="Full Name" required>
      </div>
      
      <div class="form-group">
        <label for="donorEmail">Your Email</label>
        <input type="email" id="donorEmail" name="donorEmail" placeholder="your@email.com" required>
      </div>
      
      <div class="form-group">
        <label for="donorPhone">Your Phone (Optional)</label>
        <input type="tel" id="donorPhone" name="donorPhone" placeholder="+91-XXXXXXXXXX">
      </div>
      
      <div class="form-group">
        <label for="message">Message (Optional)</label>
        <textarea id="message" name="message" placeholder="Leave a message..." rows="3"></textarea>
      </div>
      
      <div class="checkbox-group">
        <input type="checkbox" id="isAnonymous" name="isAnonymous">
        <label for="isAnonymous" style="margin: 0;">Make this donation anonymous</label>
      </div>
      
      <div class="form-group" id="anonymousNameGroup" style="display: none;">
        <label for="anonymousDonorName">Display Name</label>
        <input type="text" id="anonymousDonorName" name="anonymousDonorName" placeholder="How should we credit this donation?">
      </div>
      
      <div class="error-message" id="errorMessage"></div>
      <div class="success-message" id="successMessage"></div>
      
      <button type="submit" class="btn-donate" id="submitBtn">Proceed to Payment</button>
      
      <div class="loading" id="loadingSpinner">
        <div class="spinner"></div>
        <p>Processing donation...</p>
      </div>
    </form>
  </div>
  
  <!-- Razorpay Script -->
  <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
  
  <script>
    const form = document.getElementById('donationForm');
    const amountInput = document.getElementById('amount');
    const amountDisplay = document.getElementById('amountDisplay');
    const isAnonymousCheckbox = document.getElementById('isAnonymous');
    const anonymousNameGroup = document.getElementById('anonymousNameGroup');
    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const submitBtn = document.getElementById('submitBtn');
    
    // Amount preset buttons
    document.querySelectorAll('.amount-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        const amount = btn.dataset.amount;
        
        document.querySelectorAll('.amount-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        if (amount !== 'custom') {
          amountInput.value = amount;
        } else {
          amountInput.value = '';
          amountInput.focus();
        }
        
        updateAmountDisplay();
      });
    });
    
    // Update amount display
    amountInput.addEventListener('input', updateAmountDisplay);
    
    function updateAmountDisplay() {
      const amount = parseFloat(amountInput.value) || 0;
      amountDisplay.textContent = `₹${amount.toFixed(2)}`;
    }
    
    // Anonymous checkbox
    isAnonymousCheckbox.addEventListener('change', () => {
      anonymousNameGroup.style.display = isAnonymousCheckbox.checked ? 'block' : 'none';
    });
    
    // Form submission
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      
      const amount = parseFloat(amountInput.value);
      const donorName = document.getElementById('donorName').value;
      const donorEmail = document.getElementById('donorEmail').value;
      const donorPhone = document.getElementById('donorPhone').value;
      const message = document.getElementById('message').value;
      const isAnonymous = isAnonymousCheckbox.checked;
      const anonymousDonorName = document.getElementById('anonymousDonorName').value;
      
      // Validation
      if (amount < 1) {
        showError('Minimum donation amount is ₹1');
        return;
      }
      
      if (amount > 100000) {
        showError('Maximum donation amount is ₹100000');
        return;
      }
      
      if (!donorName || !donorEmail) {
        showError('Please fill in all required fields');
        return;
      }
      
      // Show loading
      loadingSpinner.style.display = 'block';
      submitBtn.disabled = true;
      errorMessage.style.display = 'none';
      
      try {
        // Step 1: Initiate donation
        const initiateResponse = await fetch('/api/donations/money/initiate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            amount,
            donorName,
            donorEmail,
            donorPhone,
            message,
            isAnonymous,
            anonymousDonorName: isAnonymous ? anonymousDonorName : null,
            paymentMethod: 'RAZORPAY'
          })
        });
        
        if (!initiateResponse.ok) {
          throw new Error('Failed to initiate donation');
        }
        
        const initiateData = await initiateResponse.json();
        
        if (!initiateData.success) {
          throw new Error(initiateData.message || 'Failed to initiate donation');
        }
        
        const { donationId, order, razorpayKeyId } = initiateData.data;
        
        // Step 2: Open Razorpay checkout
        openRazorpayCheckout(
          razorpayKeyId,
          order.orderId,
          amount,
          donorEmail,
          donorName,
          donationId
        );
        
      } catch (error) {
        showError(error.message || 'Failed to process donation');
      } finally {
        loadingSpinner.style.display = 'none';
        submitBtn.disabled = false;
      }
    });
    
    function openRazorpayCheckout(keyId, orderId, amount, email, name, donationId) {
      const options = {
        key: keyId,
        amount: Math.round(amount * 100),
        currency: 'INR',
        order_id: orderId,
        name: 'CareShare',
        description: `Donation - ${name}`,
        prefill: {
          name: name,
          email: email
        },
        handler: function(response) {
          verifyPayment(response, donationId);
        },
        modal: {
          ondismiss: function() {
            showError('Payment cancelled');
          }
        }
      };
      
      const rzp = new Razorpay(options);
      rzp.open();
    }
    
    async function verifyPayment(response, donationId) {
      try {
        loadingSpinner.style.display = 'block';
        
        const verifyResponse = await fetch('/api/donations/money/verify-payment', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            donationId: donationId,
            orderId: response.razorpay_order_id,
            paymentId: response.razorpay_payment_id,
            signature: response.razorpay_signature
          })
        });
        
        const verifyData = await verifyResponse.json();
        
        if (!verifyData.success) {
          throw new Error(verifyData.message || 'Payment verification failed');
        }
        
        // Payment successful
        showSuccess('Thank you! Your donation has been received. Receipt will be downloaded shortly.');
        
        // Redirect or show receipt
        setTimeout(() => {
          if (verifyData.data.receiptUrl) {
            window.location.href = verifyData.data.receiptUrl;
          } else {
            window.location.href = '/dashboard';
          }
        }, 2000);
        
      } catch (error) {
        showError('Payment verification failed: ' + error.message);
      } finally {
        loadingSpinner.style.display = 'none';
      }
    }
    
    function showError(message) {
      errorMessage.textContent = message;
      errorMessage.style.display = 'block';
      successMessage.style.display = 'none';
    }
    
    function showSuccess(message) {
      successMessage.textContent = message;
      successMessage.style.display = 'block';
      errorMessage.style.display = 'none';
    }
  </script>
</body>
</html>
```

### 4.2 User Dashboard Update
Add a section in the user dashboard to show their donations:
```html
<!-- Add this to your user dashboard template -->
<div class="donations-section">
  <h3>💝 My Donations</h3>
  <div id="donationsList"></div>
  <a href="/donate-money" class="btn btn-primary">Make a Donation</a>
</div>

<script>
  // Fetch and display user donations
  async function loadMyDonations() {
    try {
      const response = await fetch('/api/donations/money/my-donations', {
        headers: {
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
      });
      
      const data = await response.json();
      
      if (data.success && data.data.length > 0) {
        const html = data.data.map(donation => `
          <div class="donation-card">
            <div class="donation-info">
              <strong>₹${parseFloat(donation.amount).toFixed(2)}</strong>
              <span class="status ${donation.paymentStatus.toLowerCase()}">${donation.paymentStatus}</span>
              <small>${new Date(donation.createdAt).toLocaleDateString()}</small>
            </div>
            <div class="donation-actions">
              <button onclick="downloadReceipt('${donation.id}')" class="btn btn-sm btn-primary">
                Download Receipt
              </button>
            </div>
          </div>
        `).join('');
        
        document.getElementById('donationsList').innerHTML = html;
      }
    } catch (error) {
      console.error('Failed to load donations:', error);
    }
  }
  
  function downloadReceipt(donationId) {
    window.location.href = `/api/donations/money/download/${donationId}`;
  }
  
  // Load on page load
  loadMyDonations();
</script>
```

---

## 5. ADMIN PANEL UPDATES

### 5.1 Admin Dashboard: Add Money Donations Section
Add to admin dashboard template:
```html
<!-- Add this section to admin dashboard -->
<div class="admin-money-donations">
  <h2>💰 Money Donations</h2>
  
  <div class="stats-cards">
    <div class="stat-card">
      <h4>Total Donations</h4>
      <p class="stat-value" id="totalDonations">0</p>
    </div>
    <div class="stat-card">
      <h4>Total Amount</h4>
      <p class="stat-value" id="totalAmount">₹0</p>
    </div>
    <div class="stat-card">
      <h4>Successful Donations</h4>
      <p class="stat-value" id="successfulDonations">0</p>
    </div>
    <div class="stat-card">
      <h4>Average Donation</h4>
      <p class="stat-value" id="averageDonation">₹0</p>
    </div>
  </div>
  
  <div class="donations-table">
    <h3>Recent Donations</h3>
    <table>
      <thead>
        <tr>
          <th>Receipt #</th>
          <th>Donor Name</th>
          <th>Amount</th>
          <th>Status</th>
          <th>Date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody id="donationsTableBody">
        <tr><td colspan="6">Loading...</td></tr>
      </tbody>
    </table>
  </div>
</div>

<script>
  async function loadDonationStats() {
    try {
      const statsResponse = await fetch('/api/admin/money-donations/stats', {
        headers: {
          'Authorization': 'Bearer ' + localStorage.getItem('adminToken')
        }
      });
      
      const statsData = await statsResponse.json();
      
      if (statsData.success) {
        const stats = statsData.data;
        document.getElementById('totalDonations').textContent = stats.totalDonations;
        document.getElementById('totalAmount').textContent = '₹' + parseFloat(stats.totalAmount).toFixed(2);
        document.getElementById('successfulDonations').textContent = stats.successfulDonations;
        document.getElementById('averageDonation').textContent = '₹' + parseFloat(stats.averageDonation).toFixed(2);
      }
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  }
  
  async function loadDonations() {
    try {
      const donationsResponse = await fetch('/api/admin/money-donations?limit=20&offset=0', {
        headers: {
          'Authorization': 'Bearer ' + localStorage.getItem('adminToken')
        }
      });
      
      const donationsData = await donationsResponse.json();
      
      if (donationsData.success && donationsData.data.length > 0) {
        const html = donationsData.data.map(donation => `
          <tr>
            <td>${donation.receiptNumber}</td>
            <td>${donation.isAnonymous ? 'Anonymous' : donation.donorName}</td>
            <td>₹${parseFloat(donation.amount).toFixed(2)}</td>
            <td><span class="badge ${donation.paymentStatus.toLowerCase()}">${donation.paymentStatus}</span></td>
            <td>${new Date(donation.createdAt).toLocaleDateString()}</td>
            <td>
              <button onclick="viewDonation('${donation.id}')" class="btn btn-sm btn-info">View</button>
              ${donation.paymentStatus === 'SUCCESS' ? `
                <button onclick="refundDonation('${donation.id}')" class="btn btn-sm btn-danger">Refund</button>
              ` : ''}
            </td>
          </tr>
        `).join('');
        
        document.getElementById('donationsTableBody').innerHTML = html;
      }
    } catch (error) {
      console.error('Failed to load donations:', error);
    }
  }
  
  async function refundDonation(donationId) {
    if (!confirm('Are you sure you want to refund this donation?')) return;
    
    const reason = prompt('Enter refund reason:');
    if (!reason) return;
    
    try {
      const response = await fetch(`/api/admin/money-donations/${donationId}/refund`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('adminToken')
        },
        body: JSON.stringify({ reason })
      });
      
      const data = await response.json();
      
      if (data.success) {
        alert('Refund processed successfully');
        loadDonations();
        loadDonationStats();
      } else {
        alert('Failed to process refund: ' + data.message);
      }
    } catch (error) {
      alert('Error processing refund: ' + error.message);
    }
  }
  
  // Load on page load
  loadDonationStats();
  loadDonations();
</script>
```

---

## 6. DATABASE MIGRATION SCRIPT

### 6.1 Create Migration Script: `scripts/migrate-money-donation.js`
```javascript
const sequelize = require('../config/db.config');
const { DataTypes } = require('sequelize');

async function runMigration() {
  try {
    console.log('Starting money donation table migration...');
    
    // Create table
    await sequelize.query(`
      CREATE TABLE IF NOT EXISTS MoneyDonations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        donorName VARCHAR(255) NOT NULL,
        donorEmail VARCHAR(255) NOT NULL,
        donorPhone VARCHAR(20),
        amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 1),
        currency ENUM('INR') DEFAULT 'INR' NOT NULL,
        paymentMethod ENUM('RAZORPAY', 'STRIPE', 'PAYPAL') NOT NULL,
        transactionId VARCHAR(255) UNIQUE NOT NULL,
        paymentId VARCHAR(255),
        paymentSignature VARCHAR(255),
        paymentStatus ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING' NOT NULL,
        receiptNumber VARCHAR(255) UNIQUE NOT NULL,
        receiptDownloadUrl TEXT,
        message LONGTEXT,
        isAnonymous BOOLEAN DEFAULT false NOT NULL,
        anonymousDonorName VARCHAR(255),
        userId BIGINT,
        receiptGeneratedAt DATETIME,
        receiptExpiry DATETIME,
        ipAddress VARCHAR(45),
        metadata JSON,
        createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
        updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (userId) REFERENCES Users(id) ON DELETE SET NULL,
        INDEX idx_status (paymentStatus),
        INDEX idx_userId (userId),
        INDEX idx_createdAt (createdAt)
      )
    `);
    
    console.log('✅ Money donation table created successfully!');
  } catch (error) {
    console.error('❌ Migration error:', error);
    process.exit(1);
  }
}

runMigration();
```

---

## 7. IMPLEMENTATION CHECKLIST

### Phase 1: Setup & Configuration
- [ ] Install Razorpay package: `npm install razorpay`
- [ ] Add pdfkit to dependencies: `npm install pdfkit` (move from devDependencies)
- [ ] Create `.env` variables for Razorpay and company details
- [ ] Create `/uploads/receipts` directory

### Phase 2: Backend - Models & Database
- [ ] Create `models/moneyDonation.model.js`
- [ ] Update `models/index.js` with MoneyDonation relationships
- [ ] Run migration script: `node scripts/migrate-money-donation.js`

### Phase 3: Backend - Services
- [ ] Create `services/moneyDonation.service.js`
- [ ] Create `services/payment.service.js`
- [ ] Create `services/receipt.service.js`

### Phase 4: Backend - Utilities
- [ ] Create `utils/payment-utils.js`
- [ ] Create `utils/receipt-generator.js`

### Phase 5: Backend - API Layer
- [ ] Create `config/payment.config.js`
- [ ] Create `controllers/moneyDonation.controller.js`
- [ ] Create `routes/moneyDonation.routes.js`
- [ ] Update `controllers/admin.controller.js` with donation methods
- [ ] Update `routes/admin.routes.js` with donation routes
- [ ] Update `server.js` to include money donation routes

### Phase 6: Frontend - User Interface
- [ ] Create `src/main/resources/templates/donate-money.html`
- [ ] Update user dashboard with donation section
- [ ] Add donation link to navigation/menu
- [ ] Implement Razorpay integration frontend code

### Phase 7: Admin Panel
- [ ] Add money donations section to admin dashboard
- [ ] Implement donation statistics display
- [ ] Implement refund functionality UI
- [ ] Add donation filtering and search

### Phase 8: Testing & Validation
- [ ] Test donation flow end-to-end
- [ ] Test Razorpay payment integration (use test mode)
- [ ] Test PDF receipt generation
- [ ] Test email receipt sending
- [ ] Test admin refund functionality
- [ ] Test error handling and edge cases

### Phase 9: Security & Production
- [ ] Set up Razorpay webhook verification
- [ ] Configure CORS for payment endpoints
- [ ] Add rate limiting to payment routes
- [ ] Test with real Razorpay credentials (in production)
- [ ] Set up payment logging and monitoring
- [ ] Implement PCI compliance measures

---

## 8. ENVIRONMENT VARIABLES TEMPLATE

Add to `.env`:
```
# Razorpay Configuration
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxxxxxxxxxxx

# Company Details
COMPANY_NAME=CareShare
COMPANY_ADDRESS=123 Care Street, Charity City, CC 12345
COMPANY_EMAIL=contact@careshare.com
COMPANY_PHONE=+91-9876543210
COMPANY_REGISTRATION_NUMBER=REG123456
COMPANY_TAX_ID=29ABCDE1234F1Z0

# PDF & Receipt Settings
RECEIPT_LOGO_PATH=./assets/images/logo.png
PDF_GENERATION_TIMEOUT=30000

# Payment Limits
MIN_DONATION_AMOUNT=1
MAX_DONATION_AMOUNT=100000
```

---

## 9. API ENDPOINTS SUMMARY

### User Endpoints
- `POST /api/donations/money/initiate` - Initiate donation & create payment order
- `POST /api/donations/money/verify-payment` - Verify & process payment
- `GET /api/donations/money/details/:donationId` - Get donation details
- `GET /api/donations/money/download/:donationId` - Download receipt PDF
- `GET /api/donations/money/my-donations` - Get user's donations

### Admin Endpoints
- `GET /api/admin/money-donations` - List all donations
- `GET /api/admin/money-donations/stats` - Get donation statistics
- `POST /api/admin/money-donations/:donationId/refund` - Refund a donation

### Webhook
- `POST /api/donations/money/webhook/razorpay` - Razorpay webhook handler

---

## 10. TESTING RECOMMENDATIONS

### Unit Tests
- Test payment validation (amount, currency)
- Test receipt generation
- Test Razorpay signature verification

### Integration Tests
- Test complete donation flow
- Test payment confirmation flow
- Test refund workflow
- Test admin dashboard updates

### Manual Testing
Use **Razorpay Test Credentials** during development:
- Test Card: `4111 1111 1111 1111`
- Expiry: Any future date
- CVV: Any 3 digits

---

## 11. SECURITY CONSIDERATIONS

1. **PCI Compliance**: Never store raw card data; always use Razorpay
2. **Signature Verification**: Always verify Razorpay signatures
3. **Rate Limiting**: Implement rate limiting on payment endpoints
4. **Input Validation**: Validate all user inputs (amount, email, etc.)
5. **HTTPS Only**: Ensure all payment endpoints use HTTPS
6. **Secure Secrets**: Store Razorpay keys in environment variables
7. **Webhook Verification**: Verify webhook signatures before processing
8. **Logging**: Log all payment transactions for audit purposes
9. **Error Handling**: Don't expose sensitive information in error messages

---

## 12. FUTURE ENHANCEMENTS

- [ ] Multiple payment gateway support (Stripe, PayPal)
- [ ] Recurring/subscription donations
- [ ] Donation campaigns with goals
- [ ] Tax receipt certificates
- [ ] Donor recognition board
- [ ] Email confirmation with receipt attachment
- [ ] Mobile app integration
- [ ] Cryptocurrency donations
- [ ] Corporate donation matching
- [ ] Donation history export (CSV/Excel)

---

This comprehensive plan covers all aspects of implementing the money donation feature. Follow the checklist sequentially for best results!
