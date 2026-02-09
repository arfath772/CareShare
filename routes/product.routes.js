const express = require('express');
const router = express.Router();
const productController = require('../controllers/product.controller');
const { authenticateToken } = require('../middleware/auth.middleware');
const { uploadProduct, multerErrorHandler } = require('../middleware/upload.middleware');

// Product routes
router.post('/add', 
  authenticateToken, 
  uploadProduct.array('images', 12), 
  multerErrorHandler,
  productController.addProduct
);

router.get('/my-products', authenticateToken, productController.getMyProducts);
router.get('/my-products/:status', authenticateToken, productController.getMyProductsByStatus);
router.get('/available', productController.getAvailableProducts);
router.get('/available/:type', productController.getAvailableProductsByType);
router.get('/:id', productController.getProductById);

module.exports = router;
