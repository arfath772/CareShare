const express = require('express');
const router = express.Router();
const donateController = require('../controllers/donate.controller');
const { authenticateToken } = require('../middleware/auth.middleware');
const { uploadDonation, multerErrorHandler } = require('../middleware/upload.middleware');

// Donation routes
router.post('/add',
  authenticateToken,
  uploadDonation.array('images', 12),
  multerErrorHandler,
  donateController.addDonation
);

router.get('/available', donateController.getAvailableDonations);
router.get('/my-donations', authenticateToken, donateController.getMyDonations);
router.get('/:id/image/:index', donateController.getDonationImage);

// Donation request routes (specific routes before generic :id)
router.post('/request', authenticateToken, donateController.createRequest);
router.get('/my-requests', authenticateToken, donateController.getMyRequests);
router.get('/details/:id', donateController.getDonationById);

// Generic routes (after specific ones)
router.get('/:id', donateController.getDonationById);
router.delete('/:id', authenticateToken, donateController.deleteDonation);

module.exports = router;
