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
router.get('/:id', donateController.getDonationById);
router.get('/details/:id', donateController.getDonationById);
router.delete('/:id', authenticateToken, donateController.deleteDonation);

// Donation request routes
router.post('/request', authenticateToken, donateController.createRequest);
router.get('/my-requests', authenticateToken, donateController.getMyRequests);

module.exports = router;
