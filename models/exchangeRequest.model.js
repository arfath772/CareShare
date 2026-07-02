const mongoose = require('mongoose');

const exchangeRequestSchema = new mongoose.Schema({
  targetProductId: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
  exchangeItemName: { type: String, required: true },
  exchangeItemCategory: { type: String, required: true },
  exchangeItemDescription: String,
  exchangeItemImages: { type: [String], default: [] },
  additionalMessage: String,
  requesterId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  status: { type: String, default: 'PENDING' },
  rejectionReason: String
}, { timestamps: true });

module.exports = mongoose.model('ExchangeRequest', exchangeRequestSchema);
