const mongoose = require('mongoose');

const donateRequestSchema = new mongoose.Schema({
  donationId: { type: mongoose.Schema.Types.ObjectId, ref: 'DonateItem', required: true },
  receiverUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  status: { type: String, enum: ['PENDING', 'APPROVED', 'REJECTED'], default: 'PENDING' },
  description: String,
  rejectionReason: String,
  quantity: { type: Number, default: 1, min: 1 }
}, {
  timestamps: true,
  toJSON: {
    virtuals: true,
    transform: (_doc, ret) => {
      ret.id = ret._id.toString();
      return ret;
    }
  },
  toObject: { virtuals: true }
});

module.exports = mongoose.model('DonateRequest', donateRequestSchema);
