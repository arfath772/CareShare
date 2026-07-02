const express = require('express');
const router = express.Router();
const moneyDonationController = require('../controllers/moneyDonation.controller');
const { authenticateToken, optionalAuth } = require('../middleware/auth.middleware');

// Public routes (with optional authentication to track user)
router.post('/initiate', optionalAuth, moneyDonationController.initiateDonation);
router.post('/verify-payment', optionalAuth, moneyDonationController.verifyPayment);
router.post('/webhook/razorpay', moneyDonationController.handleRazorpayWebhook);

// Authenticated routes
router.get('/details/:donationId', authenticateToken, moneyDonationController.getDonationDetails);
router.get('/download/:donationId', authenticateToken, moneyDonationController.downloadReceipt);
router.get('/my-donations', authenticateToken, moneyDonationController.getUserDonations);

module.exports = router;
