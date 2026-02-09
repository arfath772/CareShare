// Middleware to verify admin role
const isAdmin = (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({ message: 'Authentication required' });
  }

  if (!req.user.isAdmin) {
    return res.status(403).json({ message: 'Access denied. Admin privileges required.' });
  }

  next();
};

// Middleware to check if user has ADMIN role
const hasAdminRole = (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({ message: 'Authentication required' });
  }

  const roles = req.user.roles || [];
  if (!roles.includes('ROLE_ADMIN') && !req.user.isAdmin) {
    return res.status(403).json({ message: 'Access denied. Admin role required.' });
  }

  next();
};

module.exports = {
  isAdmin,
  hasAdminRole
};
