const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');
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
       .moveTo(margin, doc.y)
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
      process.env.COMPANY_ADDRESS || '123, Giving Street, Kindness City, India',
      `Email: ${process.env.COMPANY_EMAIL || 'contact@careshare.com'}`,
      `Phone: ${process.env.COMPANY_PHONE || '+91 913611658'}`,
      `Registration: ${process.env.COMPANY_REGISTRATION_NUMBER || 'CS-REG-2024-5678'}`,
      `Tax ID: ${process.env.COMPANY_TAX_ID || 'PAN: AABCX1234K'}`
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
       .text(`Payment ID: ${donationData.paymentId || 'N/A'}`);

    doc.moveDown(0.3);
    doc.text(`Signature: ${donationData.paymentSignature ? donationData.paymentSignature.substring(0, 20) + '...' : 'N/A'}`);
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

    doc.moveDown(1);
    doc.fontSize(this.fontSize.small)
       .fillColor(this.colors.secondary)
       .font('Helvetica-Bold')
       .text('Tax Exemption Notice:', { underlined: true });
    
    doc.fontSize(this.fontSize.small)
       .font('Helvetica')
       .fillColor(this.colors.text)
       .text('Donations to CareShare are eligible for tax exemption under Section 80G of the Income Tax Act, 1961. This receipt is valid for tax deduction purposes.');
  }

  _addFooter(doc) {
    const pageWidth = doc.page.width;
    const margin = doc.page.margins.left;
    const footerY = doc.page.height - 100;

    // Signature Area
    doc.fontSize(this.fontSize.normal)
       .font('Helvetica-Bold')
       .text('Authorized Signatory', pageWidth - margin - 150, footerY - 20, { width: 150, align: 'center' });
    
    doc.strokeColor(this.colors.text)
       .lineWidth(0.5)
       .moveTo(pageWidth - margin - 150, footerY - 25)
       .lineTo(pageWidth - margin, footerY - 25)
       .stroke();

    // Horizontal line
    doc.moveTo(margin, footerY)
       .lineTo(pageWidth - margin, footerY)
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
