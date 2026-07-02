const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
  name: { type: String, required: true },
  price: { type: Number, required: true },
  category: { type: String, required: true },
  type: { type: String, required: true },
  description: String,
  imagePath: String,
  imagePaths: { type: [String], default: [] },
  status: { type: String, enum: ['PENDING', 'APPROVED', 'REJECTED', 'SOLD'], default: 'PENDING' },
  productCondition: { type: String, required: true },
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  approvedAt: Date,
  rejectedAt: Date,
  rejectionReason: String
}, { 
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

productSchema.virtual('user').get(function() {
  return this.userId;
});

module.exports = mongoose.model('Product', productSchema);
