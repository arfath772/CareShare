const express = require('express');
const router = express.Router();
const purchaseController = require('../controllers/purchase.controller');
const { authenticateToken } = require('../middleware/auth.middleware');

// Purchase routes
router.post('/create', authenticateToken, purchaseController.createPurchase);
router.get('/my-purchases', authenticateToken, purchaseController.getMyPurchases);
router.get('/my-sales', authenticateToken, purchaseController.getMySales);
router.put('/:purchaseId/status', authenticateToken, purchaseController.updatePurchaseStatus);

module.exports = router;
