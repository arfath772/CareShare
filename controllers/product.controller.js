const productService = require('../services/product.service');

class ProductController {
  // Add product
  async addProduct(req, res) {
    try {
      const { name, price, category, type, description, condition } = req.body;
      const files = req.files;

      if (!name || !price || !category || !type || !condition) {
        return res.status(400).json({ message: 'All required fields must be provided' });
      }

      if (!files || files.length === 0) {
        return res.status(400).json({ message: 'Please upload at least one product image' });
      }

      const productData = {
        name,
        price: parseFloat(price),
        category,
        type,
        description: description || '',
        condition
      };

      const product = await productService.addProduct(productData, files, req.user.id);

      return res.json({
        message: `Product submitted successfully with ${files.length} images. Waiting for admin approval.`,
        product,
        totalImages: files.length
      });
    } catch (error) {
      console.error('Add product error:', error);
      return res.status(400).json({ message: 'Error adding product: ' + error.message });
    }
  }

  // Get user's products
  async getMyProducts(req, res) {
    try {
      const products = await productService.getUserProducts(req.user.id);
      return res.json(products);
    } catch (error) {
      console.error('Get my products error:', error);
      return res.status(400).json({ message: 'Error fetching products: ' + error.message });
    }
  }

  // Get user's products by status
  async getMyProductsByStatus(req, res) {
    try {
      const { status } = req.params;
      const validStatuses = ['PENDING', 'APPROVED', 'REJECTED', 'SOLD'];

      if (!validStatuses.includes(status.toUpperCase())) {
        return res.status(400).json({ message: 'Invalid status value' });
      }

      const products = await productService.getUserProductsByStatus(req.user.id, status.toUpperCase());
      return res.json(products);
    } catch (error) {
      console.error('Get products by status error:', error);
      return res.status(400).json({ message: 'Error fetching products: ' + error.message });
    }
  }

  // Get available products
  async getAvailableProducts(req, res) {
    try {
      const { type, category, sort } = req.query;

      let products = await productService.getApprovedProducts(type, category);

      // Sort products
      if (sort) {
        products = this.sortProducts(products, sort);
      }

      return res.json(products);
    } catch (error) {
      console.error('Get available products error:', error);
      return res.status(500).json({ error: 'Failed to load products' });
    }
  }

  // Get available products by type
  async getAvailableProductsByType(req, res) {
    try {
      const { type } = req.params;
      const products = await productService.getApprovedProducts(type);
      return res.json(products);
    } catch (error) {
      console.error('Get products by type error:', error);
      return res.status(500).json({ error: 'Failed to load products' });
    }
  }

  // Get product by ID
  async getProductById(req, res) {
    try {
      const { id } = req.params;
      const product = await productService.getProductById(id);

      if (!product) {
        return res.status(404).json({ error: 'Product not found or not approved' });
      }

      return res.json(product);
    } catch (error) {
      console.error('Get product error:', error);
      return res.status(500).json({ error: 'Failed to load product details' });
    }
  }

  // Sort products
  sortProducts(products, sortType) {
    switch (sortType) {
      case 'newest':
        return products.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      case 'oldest':
        return products.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
      case 'price_low':
        return products.sort((a, b) => a.price - b.price);
      case 'price_high':
        return products.sort((a, b) => b.price - a.price);
      case 'name_asc':
        return products.sort((a, b) => a.name.localeCompare(b.name));
      case 'name_desc':
        return products.sort((a, b) => b.name.localeCompare(a.name));
      default:
        return products;
    }
  }
}

module.exports = new ProductController();
