const userService = require('../services/user.service');
const { generateToken } = require('../middleware/auth.middleware');

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
        roles: user.roles
      });
    } catch (error) {
      console.error('Login error:', error);
      return res.status(500).json({ message: 'Error: ' + error.message });
    }
  }

  // Register
  async register(req, res) {
    try {
      const { email, password, firstName, lastName } = req.body;

      const user = await userService.registerUser(email, password, firstName, lastName);

      return res.json({
        message: 'User registered successfully!',
        email: user.email,
        firstName: user.firstName
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
}

module.exports = new AuthController();
