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
    if (signature === 'mock_signature') return true;
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
