const userService = require('../services/user.service');
const { generateToken } = require('../middleware/auth.middleware');
const { User } = require('../models');

const buildNgoDocumentsFromFiles = (files = {}) => {
  const documentMap = {};

  Object.keys(files).forEach((key) => {
    const uploaded = files[key] && files[key][0];
    if (uploaded) {
      documentMap[key] = {
        storage: 'mongodb',
        filename: uploaded.originalname,
        contentType: uploaded.mimetype,
        data: uploaded.buffer,
        uploadedAt: new Date()
      };
    }
  });

  return documentMap;
};

class AuthController {
  // Login
  async login(req, res) {
    try {
      const { email, password } = req.body;

      if (!email || !password) {
        return res.status(400).json({ message: 'Email and password are required' });
      }

      // Find user
      const user = await userService.findByEmail(email);
      if (!user) {
        return res.status(401).json({ message: 'Invalid email or password' });
      }

      // Validate password
      const isValidPassword = await user.validatePassword(password);
      if (!isValidPassword) {
        return res.status(401).json({ message: 'Invalid email or password' });
      }

      if (user.accountType === 'NGO' && user.ngoStatus !== 'APPROVED') {
        const statusMessage = user.ngoStatus === 'REJECTED'
          ? `NGO account rejected: ${user.ngoRejectionReason || 'Please contact admin'}`
          : 'NGO account is pending admin approval';

        return res.status(403).json({ message: statusMessage });
      }

      // Generate JWT token
      const token = generateToken(user);

      // Set HTTP-only cookie
      res.cookie('jwtToken', token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        maxAge: 24 * 60 * 60 * 1000, // 24 hours
        path: '/'
      });

      return res.json({
        token,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        isAdmin: user.isAdmin,
        roles: user.roles,
        accountType: user.accountType,
        ngoStatus: user.ngoStatus
      });
    } catch (error) {
      console.error('Login error:', error);
      return res.status(500).json({ message: 'Error: ' + error.message });
    }
  }

  // Register
  async register(req, res) {
    try {
      const {
        email,
        password,
        firstName,
        lastName,
        accountType,
        ngoLegalName,
        ngoDarpanId,
        ngoPan,
        ngoOfficeAddress,
        hasFcra
      } = req.body;

      const ngoDocuments = buildNgoDocumentsFromFiles(req.files);

      const user = await userService.registerUser(email, password, firstName, lastName, {
        accountType,
        ngoLegalName,
        ngoDarpanId,
        ngoPan,
        ngoOfficeAddress,
        ngoDocuments,
        hasFcra: String(hasFcra).toLowerCase() === 'true'
      });

      const isNgo = user.accountType === 'NGO';

      return res.json({
        message: isNgo
          ? 'NGO registered successfully. You can login after admin approval.'
          : 'User registered successfully!',
        email: user.email,
        firstName: user.firstName,
        accountType: user.accountType,
        ngoStatus: user.ngoStatus
      });
    } catch (error) {
      console.error('Registration error:', error);
      return res.status(400).json({ message: 'Error: ' + error.message });
    }
  }

  // Logout
  async logout(req, res) {
    try {
      res.clearCookie('jwtToken', { path: '/' });
      return res.json({ message: 'Logged out successfully' });
    } catch (error) {
      return res.status(500).json({ message: 'Error: ' + error.message });
    }
  }

  // Forgot password
  async forgotPassword(req, res) {
    try {
      const { email } = req.body;

      if (!email) {
        return res.status(400).json({
          message: 'Email is required',
          success: false
        });
      }

      // Always return success message for security
      try {
        await userService.createPasswordResetToken(email);
      } catch (error) {
        // Don't reveal if email exists
        console.error('Forgot password error:', error);
      }

      return res.json({
        message: 'If this email exists in our system, a password reset link will be sent.',
        success: true
      });
    } catch (error) {
      return res.status(400).json({
        message: 'Error processing request. Please try again.',
        success: false
      });
    }
  }

  // Reset password
  async resetPassword(req, res) {
    try {
      const { token, newPassword, confirmPassword } = req.body;

      if (!token || !newPassword || !confirmPassword) {
        return res.status(400).json({
          message: 'All fields are required',
          success: false
        });
      }

      if (newPassword !== confirmPassword) {
        return res.status(400).json({
          message: 'Passwords do not match',
          success: false
        });
      }

      const isValid = await userService.validateResetToken(token);
      if (!isValid) {
        return res.status(400).json({
          message: 'Invalid or expired reset token',
          success: false
        });
      }

      await userService.resetPassword(token, newPassword);

      return res.json({
        message: 'Password reset successfully',
        success: true
      });
    } catch (error) {
      console.error('Reset password error:', error);
      return res.status(400).json({
        message: 'Error resetting password: ' + error.message,
        success: false
      });
    }
  }

  // Validate reset token
  async validateResetToken(req, res) {
    try {
      const { token } = req.query;

      if (!token) {
        return res.status(400).json({
          message: 'Token is required',
          success: false
        });
      }

      const isValid = await userService.validateResetToken(token);

      if (isValid) {
        return res.json({
          message: 'Token is valid',
          success: true
        });
      } else {
        return res.status(400).json({
          message: 'Invalid or expired token',
          success: false
        });
      }
    } catch (error) {
      return res.status(400).json({
        message: 'Error validating token',
        success: false
      });
    }
  }

  // Stream NGO document (owner/admin only)
  async getNgoDocument(req, res) {
    try {
      const { userId, docKey } = req.params;
      const requester = req.user;

      if (!requester) {
        return res.status(401).json({ message: 'Authentication required' });
      }

      const isOwner = String(requester._id || requester.id) === String(userId);
      const isAdmin = !!requester.isAdmin;
      if (!isOwner && !isAdmin) {
        return res.status(403).json({ message: 'Not authorized to view this document' });
      }

      const user = await User.findById(userId).select('ngoDocuments');
      if (!user || !user.ngoDocuments || !user.ngoDocuments[docKey]) {
        return res.status(404).json({ message: 'Document not found' });
      }

      const doc = user.ngoDocuments[docKey];

      if (typeof doc === 'string' && doc.trim().length > 0) {
        return res.redirect(doc);
      }

      if (doc && typeof doc === 'object' && doc.data) {
        const filename = doc.filename || `${docKey}`;
        res.setHeader('Content-Type', doc.contentType || 'application/octet-stream');
        res.setHeader('Content-Disposition', `inline; filename="${filename}"`);
        return res.send(doc.data);
      }

      return res.status(404).json({ message: 'Document not found' });
    } catch (error) {
      console.error('Get NGO document error:', error);
      return res.status(500).json({ message: 'Error retrieving NGO document' });
    }
  }
}

module.exports = new AuthController();
