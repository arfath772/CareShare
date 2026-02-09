const express = require('express');
const router = express.Router();
const userController = require('../controllers/user.controller');
const { authenticateToken } = require('../middleware/auth.middleware');

// User routes (require authentication)
router.get('/me', authenticateToken, userController.getCurrentUser);

module.exports = router;
