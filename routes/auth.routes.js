const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');
const { uploadNgoDocuments, multerErrorHandler } = require('../middleware/upload.middleware');
const { authenticateToken } = require('../middleware/auth.middleware');

const ngoDocumentFields = [
	{ name: 'registrationCertificate', maxCount: 1 },
	{ name: 'trustDeedOrMoa', maxCount: 1 },
	{ name: 'ngoPanCard', maxCount: 1 },
	{ name: 'nitiDarpanRegistration', maxCount: 1 },
	{ name: 'certificate12A', maxCount: 1 },
	{ name: 'certificate80G', maxCount: 1 },
	{ name: 'csr1Registration', maxCount: 1 },
	{ name: 'bankAccountProof', maxCount: 1 },
	{ name: 'auditedAccounts', maxCount: 1 },
	{ name: 'annualReport', maxCount: 1 },
	{ name: 'projectProposal', maxCount: 1 },
	{ name: 'fcraCertificate', maxCount: 1 },
	{ name: 'kycMembers', maxCount: 1 },
	{ name: 'officeAddressProof', maxCount: 1 },
	{ name: 'governingBodyList', maxCount: 1 }
];

// Auth routes
router.post('/login', authController.login);
router.post('/register', uploadNgoDocuments.fields(ngoDocumentFields), multerErrorHandler, authController.register);
router.post('/logout', authController.logout);
router.post('/forgot-password', authController.forgotPassword);
router.post('/reset-password', authController.resetPassword);
router.get('/validate-reset-token', authController.validateResetToken);
router.get('/ngo-documents/:userId/:docKey', authenticateToken, authController.getNgoDocument);

module.exports = router;
