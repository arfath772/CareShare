const mongoose = require('mongoose');

const donateItemSchema = new mongoose.Schema({
  itemType: { type: String, required: true },
  category: { type: String },
  itemName: { type: String, required: true },
  quantity: { type: Number, required: true },
  itemCondition: { type: String, required: true },
  pickupAddress: { type: String, required: true },
  donationImages: [{
    filename: String,
    contentType: String,
    data: Buffer
  }],
  imageUrls: String,
  mainImageUrl: String,
  status: { type: String, enum: ['PENDING', 'APPROVED', 'REJECTED', 'CLAIMED'], default: 'PENDING' },
  rejectionReason: String,
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true }
}, {
  timestamps: true,
  toJSON: {
    virtuals: true,
    transform: (_doc, ret) => {
      ret.id = ret._id.toString();

      const fallbackImageUrls = [];
      if (ret.imageUrls && typeof ret.imageUrls === 'string') {
        try {
          const parsed = JSON.parse(ret.imageUrls);
          if (Array.isArray(parsed)) {
            fallbackImageUrls.push(...parsed);
          }
        } catch (error) {
          // Keep fallback list empty on parse errors
        }
      }

      if (Array.isArray(ret.donationImages) && ret.donationImages.length > 0) {
        ret.imageUrls = ret.donationImages.map((_img, index) => `/api/donate/${ret.id}/image/${index}`);
        ret.mainImageUrl = ret.imageUrls[0];
      } else {
        ret.imageUrls = fallbackImageUrls;
      }

      ret.imageUrl = ret.mainImageUrl || ret.imageUrls[0] || null;

      ret.donor = {
        firstName: 'CareShare',
        lastName: 'Admin',
        email: 'mohammedarfath46982@gmail.com',
        phone: '9113611658',
        phoneNumber: '9113611658'
      };

      delete ret.donationImages;
      return ret;
    }
  },
  toObject: { 
    virtuals: true,
    transform: (_doc, ret) => {
      ret.id = ret._id.toString();
      ret.donor = {
        firstName: 'CareShare',
        lastName: 'Admin',
        email: 'mohammedarfath46982@gmail.com',
        phone: '9113611658',
        phoneNumber: '9113611658'
      };
      return ret;
    }
  }
});

module.exports = mongoose.model('DonateItem', donateItemSchema);
