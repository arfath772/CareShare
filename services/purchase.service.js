const { PurchaseRequest, Product, User } = require('../models');

class PurchaseService {
  // Create purchase
  async createPurchase(purchaseData, buyerEmail) {
    try {
      // Find buyer
      const buyer = await User.findOne({ email: buyerEmail });
      if (!buyer) {
        throw new Error('Buyer not found');
      }

      // Find product
      const product = await Product.findById(purchaseData.productId).populate('userId', 'firstName lastName email');

      if (!product) {
        throw new Error('Product not found');
      }

      if (product.status !== 'APPROVED') {
        throw new Error('This product is not available for purchase');
      }

      // Create purchase request
      const purchase = new PurchaseRequest({
        productId: purchaseData.productId,
        buyerId: buyer._id,
        fullName: purchaseData.fullName,
        email: purchaseData.email,
        phone: purchaseData.phone,
        shippingAddress: purchaseData.shippingAddress,
        paymentMethod: purchaseData.paymentMethod,
        amount: product.price,
        status: 'PENDING'
      });

      await purchase.save();

      return {
        success: true,
        message: 'Purchase request created successfully',
        purchase
      };
    } catch (error) {
      console.error('Error creating purchase:', error);
      throw error;
    }
  }

  // Get purchases by buyer
  async getPurchasesByBuyer(buyerEmail) {
    const buyer = await User.findOne({ email: buyerEmail });
    if (!buyer) {
      throw new Error('Buyer not found');
    }

    return await PurchaseRequest.find({ buyerId: buyer._id })
      .populate({
        path: 'productId',
        populate: { path: 'userId', select: 'firstName lastName email' }
      })
      .populate('buyerId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Get sales by seller
  async getSalesBySeller(sellerEmail) {
    const seller = await User.findOne({ email: sellerEmail });
    if (!seller) {
      throw new Error('Seller not found');
    }

    return await PurchaseRequest.find()
      .populate({
        path: 'productId',
        match: { userId: seller._id },
        populate: { path: 'userId', select: 'firstName lastName email' }
      })
      .populate('buyerId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Update purchase status
  async updatePurchaseStatus(purchaseId, status, userEmail) {
    const user = await User.findOne({ email: userEmail });
    if (!user) {
      throw new Error('User not found');
    }

    const purchase = await PurchaseRequest.findById(purchaseId)
      .populate({
        path: 'productId',
        populate: { path: 'userId' }
      });

    if (!purchase) {
      throw new Error('Purchase not found');
    }

    // Verify user is either the buyer or the seller
    const isBuyer = purchase.buyerId.toString() === user._id.toString();
    const isSeller = purchase.productId.userId._id.toString() === user._id.toString();

    if (!isBuyer && !isSeller) {
      throw new Error('You are not authorized to update this purchase');
    }

    purchase.status = status;
    await purchase.save();

    // If purchase is delivered/completed, mark product as SOLD
    if (status === 'DELIVERED') {
      const product = await Product.findById(purchase.productId._id);
      if (product) {
        product.status = 'SOLD';
        await product.save();
      }
    }

    return purchase;
  }

  // Get all purchases (admin)
  async getAllPurchases() {
    return await PurchaseRequest.find()
      .populate({
        path: 'productId',
        populate: { path: 'userId', select: 'firstName lastName email' }
      })
      .populate('buyerId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }
}

module.exports = new PurchaseService();
