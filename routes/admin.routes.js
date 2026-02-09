const express = require('express');
const router = express.Router();
const adminController = require('../controllers/admin.controller');
const { authenticateToken } = require('../middleware/auth.middleware');
const { isAdmin } = require('../middleware/admin.middleware');

// All admin routes require authentication and admin role
router.use(authenticateToken);
router.use(isAdmin);

// User management
router.get('/users', adminController.getAllUsers);
router.put('/users/:userId/role', adminController.updateUserRole);
router.delete('/users/:userId', adminController.deleteUser);

// Statistics
router.get('/stats', adminController.getAdminStats);

// Product management
router.get('/products/pending', adminController.getPendingProducts);
router.post('/products/:productId/approve', adminController.approveProduct);
router.post('/products/:productId/reject', adminController.rejectProduct);
router.get('/products/stats', adminController.getProductStats);

// Donation management
router.get('/donations/items/pending', adminController.getPendingDonatedItems);
router.get('/donations/items/all', adminController.getAllDonatedItems);
router.post('/donations/items/:id/approve', adminController.approveDonatedItem);
router.post('/donations/items/:id/reject', adminController.rejectDonatedItem);

// Donation request management
router.get('/donations/requests/pending', adminController.getPendingDonationRequests);
router.get('/donations/requests/all', adminController.getAllDonationRequests);
router.post('/donations/requests/:id/approve', adminController.approveDonationRequest);
router.post('/donations/requests/:id/reject', adminController.rejectDonationRequest);
router.get('/donations/stats', adminController.getDonationStats);

// Exchange request management
router.get('/exchange-requests', adminController.getExchangeRequests);
router.get('/exchange-requests/stats', adminController.getExchangeRequestStats);
router.put('/exchange-requests/:id/approve', adminController.approveExchangeRequest);
router.put('/exchange-requests/:id/reject', adminController.rejectExchangeRequest);
router.delete('/exchange-requests/:id', adminController.deleteExchangeRequest);

module.exports = router;
