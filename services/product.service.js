const { Product } = require('../models');
const path = require('path');
const fs = require('fs');

class ProductService {
  // Add new product
  async addProduct(productData, files, userId) {
    try {
      // Create product
      const product = new Product({
        name: productData.name,
        price: productData.price,
        category: productData.category,
        type: productData.type,
        description: productData.description || '',
        productCondition: productData.condition,
        status: 'PENDING',
        userId: userId
      });

      // Handle memory storage images
      const productImages = Array.isArray(files)
        ? files
          .filter((file) => file && file.buffer)
          .map((file) => ({
            filename: file.originalname,
            contentType: file.mimetype,
            data: file.buffer
          }))
        : [];

      product.productImages = productImages;

      if (productImages.length > 0) {
        product.imagePath = `/api/products/${product._id.toString()}/image/0`;
        product.imagePaths = productImages.map((_, index) => `/api/products/${product._id.toString()}/image/${index}`);
      } else {
        product.imagePath = '/uploads/default-product.png';
        product.imagePaths = [];
      }

      await product.save();
      return await product.populate('userId', 'firstName lastName email');
    } catch (error) {
      console.error('Error adding product:', error);
      throw new Error('Failed to add product: ' + error.message);
    }
  }

  // Get user products
  async getUserProducts(userId) {
    return await Product.find({ userId }).populate('userId', 'firstName lastName email').sort({ createdAt: -1 });
  }

  // Get user products by status
  async getUserProductsByStatus(userId, status) {
    return await Product.find({ userId, status }).populate('userId', 'firstName lastName email').sort({ createdAt: -1 });
  }

  // Get pending products
  async getPendingProducts() {
    return await Product.find({ status: 'PENDING' }).populate('userId', 'firstName lastName email').sort({ createdAt: -1 });
  }

  // Get approved products
  async getApprovedProducts(type = null, category = null) {
    const filter = { status: 'APPROVED' };
    if (type && type !== 'all') filter.type = type;
    if (category && category !== 'all') filter.category = category;

    return await Product.find(filter).populate('userId', 'firstName lastName email').sort({ createdAt: -1 });
  }

  // Get product by ID
  async getProductById(id) {
    return await Product.findById(id).populate('userId', 'firstName lastName email');
  }

  // Approve product
  async approveProduct(productId) {
    const product = await Product.findById(productId);
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
    const product = await Product.findById(productId);
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
    return await Product.countDocuments({ status: 'PENDING' });
  }

  // Get approved products count
  async getApprovedProductsCount() {
    return await Product.countDocuments({ status: 'APPROVED' });
  }

  // Get product image by index
  async getProductImage(productId, imageIndex) {
    const product = await Product.findById(productId).select('productImages');
    if (!product) {
      throw new Error('Product not found');
    }

    if (Array.isArray(product.productImages) && product.productImages.length > 0) {
      const index = Number.parseInt(imageIndex, 10);
      if (Number.isNaN(index) || index < 0 || index >= product.productImages.length) {
        throw new Error('Image not found');
      }

      const img = product.productImages[index];
      return {
        contentType: img.contentType || 'application/octet-stream',
        data: img.data
      };
    }

    throw new Error('Product has no images');
  }
}

module.exports = new ProductService();
