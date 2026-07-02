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
  rejectionReason: String,
  exchangeImages: [{
    filename: String,
    contentType: String,
    data: Buffer
  }]
}, { 
  timestamps: true,
  toJSON: {
    virtuals: true,
    transform: (doc, ret) => {
      ret.id = ret._id.toString();
      if (Array.isArray(ret.exchangeImages) && ret.exchangeImages.length > 0) {
        ret.exchangeItemImages = ret.exchangeImages.map((_, index) => `/api/exchange-requests/${ret.id}/image/${index}`);
      }
      delete ret.exchangeImages;
      return ret;
    }
  },
  toObject: {
    virtuals: true,
    transform: (doc, ret) => {
      ret.id = ret._id.toString();
      if (Array.isArray(ret.exchangeImages) && ret.exchangeImages.length > 0) {
        ret.exchangeItemImages = ret.exchangeImages.map((_, index) => `/api/exchange-requests/${ret.id}/image/${index}`);
      }
      delete ret.exchangeImages;
      return ret;
    }
  }
});

module.exports = mongoose.model('ExchangeRequest', exchangeRequestSchema);
