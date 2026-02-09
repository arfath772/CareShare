const { PurchaseRequest, Product, User } = require('../models');

class PurchaseService {
  // Create purchase
  async createPurchase(purchaseData, buyerEmail) {
    try {
      // Find buyer
      const buyer = await User.findOne({ where: { email: buyerEmail } });
      if (!buyer) {
        throw new Error('Buyer not found');
      }

      // Find product
      const product = await Product.findByPk(purchaseData.productId, {
        include: [{
          model: User,
          as: 'user',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }]
      });

      if (!product) {
        throw new Error('Product not found');
      }

      if (product.status !== 'APPROVED') {
        throw new Error('This product is not available for purchase');
      }

      // Create purchase request
      const purchase = await PurchaseRequest.create({
        productId: purchaseData.productId,
        buyerId: buyer.id,
        fullName: purchaseData.fullName,
        email: purchaseData.email,
        phone: purchaseData.phone,
        shippingAddress: purchaseData.shippingAddress,
        paymentMethod: purchaseData.paymentMethod,
        amount: product.price,
        status: 'PENDING',
        createdAt: new Date()
      });

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
    const buyer = await User.findOne({ where: { email: buyerEmail } });
    if (!buyer) {
      throw new Error('Buyer not found');
    }

    return await PurchaseRequest.findAll({
      where: { buyerId: buyer.id },
      include: [
        {
          model: Product,
          as: 'product',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'buyer',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get sales by seller
  async getSalesBySeller(sellerEmail) {
    const seller = await User.findOne({ where: { email: sellerEmail } });
    if (!seller) {
      throw new Error('Seller not found');
    }

    return await PurchaseRequest.findAll({
      include: [
        {
          model: Product,
          as: 'product',
          where: { userId: seller.id },
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'buyer',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['createdAt', 'DESC']]
    });
  }

  // Update purchase status
  async updatePurchaseStatus(purchaseId, status, userEmail) {
    const user = await User.findOne({ where: { email: userEmail } });
    if (!user) {
      throw new Error('User not found');
    }

    const purchase = await PurchaseRequest.findByPk(purchaseId, {
      include: [
        {
          model: Product,
          as: 'product',
          include: [{
            model: User,
            as: 'user'
          }]
        }
      ]
    });

    if (!purchase) {
      throw new Error('Purchase not found');
    }

    // Verify user is either the buyer or the seller
    const isBuyer = purchase.buyerId === user.id;
    const isSeller = purchase.product.userId === user.id;

    if (!isBuyer && !isSeller) {
      throw new Error('You are not authorized to update this purchase');
    }

    purchase.status = status;
    purchase.updatedAt = new Date();
    await purchase.save();

    // If purchase is delivered/completed, mark product as SOLD
    if (status === 'DELIVERED') {
      const product = await Product.findByPk(purchase.productId);
      if (product) {
        product.status = 'SOLD';
        await product.save();
      }
    }

    return purchase;
  }

  // Get all purchases (admin)
  async getAllPurchases() {
    return await PurchaseRequest.findAll({
      include: [
        {
          model: Product,
          as: 'product',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'buyer',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['createdAt', 'DESC']]
    });
  }
}

module.exports = new PurchaseService();
