const jwt = require('jsonwebtoken');
const { User } = require('../models');

// JWT utility functions
const generateToken = (user) => {
  return jwt.sign(
    { email: user.email, id: user.id },
    process.env.JWT_SECRET,
    { expiresIn: parseInt(process.env.JWT_EXPIRATION) }
  );
};

const verifyToken = (token) => {
  try {
    return jwt.verify(token, process.env.JWT_SECRET);
  } catch (error) {
    return null;
  }
};

// Middleware to verify JWT token
const authenticateToken = async (req, res, next) => {
  try {
    // Check for token in cookies or Authorization header
    let token = req.cookies?.jwtToken;
    
    if (!token) {
      const authHeader = req.headers['authorization'];
      token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN
    }

    if (!token) {
      return res.status(401).json({ message: 'No token provided, authentication required' });
    }

    const decoded = verifyToken(token);
    if (!decoded) {
      return res.status(401).json({ message: 'Invalid or expired token' });
    }

    // Validate decoded token has required fields
    if (!decoded.email || !decoded.id) {
      return res.status(401).json({ message: 'Invalid token payload' });
    }

    // Find user from decoded token
    const user = await User.findOne({
      where: { email: decoded.email },
      attributes: { exclude: ['password'] }
    });

    if (!user) {
      return res.status(401).json({ message: 'User not found' });
    }

    // Attach user to request object
    req.user = user;
    next();
  } catch (error) {
    console.error('Authentication error:', error);
    return res.status(401).json({ 
      message: 'Authentication failed',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
};

// Optional authentication (doesn't fail if no token)
const optionalAuth = async (req, res, next) => {
  try {
    let token = req.cookies?.jwtToken;
    
    if (!token) {
      const authHeader = req.headers['authorization'];
      token = authHeader && authHeader.split(' ')[1];
    }

    if (token) {
      const decoded = verifyToken(token);
      if (decoded) {
        const user = await User.findOne({
          where: { email: decoded.email },
          attributes: { exclude: ['password'] }
        });
        if (user) {
          req.user = user;
        }
      }
    }
    next();
  } catch (error) {
    next();
  }
};

module.exports = {
  generateToken,
  verifyToken,
  authenticateToken,
  optionalAuth
};
