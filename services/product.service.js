const { Product, User } = require('../models');
const path = require('path');
const fs = require('fs');

class ProductService {
  // Add new product
  async addProduct(productData, files, userId) {
    try {
      // Create product
      const product = await Product.create({
        name: productData.name,
        price: productData.price,
        category: productData.category,
        type: productData.type,
        description: productData.description || '',
        productCondition: productData.condition,
        status: 'PENDING',
        userId: userId,
        createdAt: new Date()
      });

      // Handle images
      if (files && files.length > 0) {
        const imagePaths = files.map(file => `/uploads/products/${file.filename}`);
        product.imagePath = imagePaths[0]; // First image as main
        product.imagePaths = imagePaths;
        await product.save();
      } else {
        product.imagePath = '/uploads/default-product.png';
        await product.save();
      }

      return product;
    } catch (error) {
      console.error('Error adding product:', error);
      throw new Error('Failed to add product: ' + error.message);
    }
  }

  // Get user products
  async getUserProducts(userId) {
    return await Product.findAll({
      where: { userId },
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get user products by status
  async getUserProductsByStatus(userId, status) {
    return await Product.findAll({
      where: { userId, status },
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get pending products
  async getPendingProducts() {
    return await Product.findAll({
      where: { status: 'PENDING' },
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get approved products
  async getApprovedProducts(type = null, category = null) {
    const where = { status: 'APPROVED' };
    if (type && type !== 'all') where.type = type;
    if (category && category !== 'all') where.category = category;

    return await Product.findAll({
      where,
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get product by ID
  async getProductById(id) {
    return await Product.findByPk(id, {
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }]
    });
  }

  // Approve product
  async approveProduct(productId) {
    const product = await Product.findByPk(productId);
    if (!product) {
      throw new Error('Product not found');
    }

    product.status = 'APPROVED';
    product.approvedAt = new Date();
    product.rejectionReason = null;
    await product.save();

    return product;
  }

  // Reject product
  async rejectProduct(productId, rejectionReason) {
    const product = await Product.findByPk(productId);
    if (!product) {
      throw new Error('Product not found');
    }

    product.status = 'REJECTED';
    product.rejectedAt = new Date();
    product.rejectionReason = rejectionReason;
    await product.save();

    return product;
  }

  // Get pending products count
  async getPendingProductsCount() {
    return await Product.count({ where: { status: 'PENDING' } });
  }

  // Get approved products count
  async getApprovedProductsCount() {
    return await Product.count({ where: { status: 'APPROVED' } });
  }
}

module.exports = new ProductService();
