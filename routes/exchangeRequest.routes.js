const express = require('express');
const router = express.Router();
const exchangeRequestController = require('../controllers/exchangeRequest.controller');
const { authenticateToken } = require('../middleware/auth.middleware');
const { uploadExchange, multerErrorHandler } = require('../middleware/upload.middleware');

// Exchange request routes
router.post('/submit',
  authenticateToken,
  uploadExchange.array('images', 12),
  multerErrorHandler,
  exchangeRequestController.submitExchangeRequest
);

router.get('/my-requests', authenticateToken, exchangeRequestController.getMyExchangeRequests);
router.get('/received', authenticateToken, exchangeRequestController.getReceivedExchangeRequests);
router.get('/:id', authenticateToken, exchangeRequestController.getExchangeRequestById);
router.put('/:id/accept', authenticateToken, exchangeRequestController.acceptExchangeRequest);
router.put('/:id/decline', authenticateToken, exchangeRequestController.declineExchangeRequest);
router.delete('/:id/cancel', authenticateToken, exchangeRequestController.cancelExchangeRequest);

module.exports = router;
