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
  rejectionReason: String,
  productImages: [{
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
      if (Array.isArray(ret.productImages) && ret.productImages.length > 0) {
        ret.imagePaths = ret.productImages.map((_, index) => `/api/products/${ret.id}/image/${index}`);
        ret.imagePath = ret.imagePaths[0];
      }
      delete ret.productImages;
      return ret;
    }
  },
  toObject: { 
    virtuals: true,
    transform: (doc, ret) => {
      ret.id = ret._id.toString();
      if (Array.isArray(ret.productImages) && ret.productImages.length > 0) {
        ret.imagePaths = ret.productImages.map((_, index) => `/api/products/${ret.id}/image/${index}`);
        ret.imagePath = ret.imagePaths[0];
      }
      delete ret.productImages;
      return ret;
    }
  }
});

productSchema.virtual('user').get(function() {
  return this.userId;
});

module.exports = mongoose.model('Product', productSchema);
