const { User } = require('../models');

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

  // Update current user profile
  async updateProfile(req, res) {
    try {
      if (!req.user) {
        return res.status(401).json({ message: 'User not authenticated' });
      }

      const { firstName, lastName, phone, phoneNumber } = req.body;
      const user = await User.findById(req.user.id);
      
      if (!user) {
        return res.status(404).json({ message: 'User not found' });
      }

      if (firstName !== undefined) user.firstName = firstName.trim();
      if (lastName !== undefined) user.lastName = lastName.trim();
      
      const newPhone = phone || phoneNumber;
      if (newPhone !== undefined) {
        user.phone = newPhone.trim();
        user.phoneNumber = newPhone.trim();
      }

      await user.save();
      return res.json(user);
    } catch (error) {
      console.error('Update profile error:', error);
      return res.status(400).json({ message: 'Error updating profile: ' + error.message });
    }
  }
}

module.exports = new UserController();
