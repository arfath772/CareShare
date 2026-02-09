class UserController {
  // Get current user
  async getCurrentUser(req, res) {
    try {
      if (!req.user) {
        return res.status(401).json({ message: 'User not authenticated' });
      }

      return res.json(req.user);
    } catch (error) {
      console.error('Get current user error:', error);
      return res.status(500).json({ message: 'Error: ' + error.message });
    }
  }
}

module.exports = new UserController();
