const mongoose = require('mongoose');

const moneyDonationSchema = new mongoose.Schema({
  donorName: { type: String, required: true },
  donorEmail: { type: String, required: true, lowercase: true },
  donorPhone: { type: String },
  amount: { type: mongoose.Schema.Types.Decimal128, required: true, min: 1 },
  currency: { type: String, enum: ['INR'], default: 'INR', required: true },
  paymentMethod: { type: String, enum: ['RAZORPAY', 'STRIPE', 'PAYPAL'], required: true },
  transactionId: { type: String, unique: true, required: true },
  paymentId: { type: String },
  paymentSignature: { type: String },
  paymentStatus: { type: String, enum: ['PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'], default: 'PENDING' },
  receiptNumber: { type: String, unique: true, required: true },
  receiptDownloadUrl: { type: String },
  message: { type: String },
  isAnonymous: { type: Boolean, default: false },
  anonymousDonorName: { type: String },
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
  receiptGeneratedAt: { type: Date },
  receiptExpiry: { type: Date },
  ipAddress: { type: String },
  metadata: { type: mongoose.Schema.Types.Mixed }
}, {
  timestamps: true,
  collection: 'MoneyDonations',
  toObject: { virtuals: true },
  toJSON: { virtuals: true }
});

module.exports = mongoose.model('MoneyDonation', moneyDonationSchema);
