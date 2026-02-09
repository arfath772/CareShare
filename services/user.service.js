const { User } = require('../models');
const { v4: uuidv4 } = require('uuid');
const emailService = require('./email.service');

class UserService {
  // Register new user
  async registerUser(email, password, firstName, lastName) {
    // Validate input
    if (!email || !password || !firstName || !lastName) {
      throw new Error('All fields are required');
    }

    // Check if user already exists
    const existingUser = await User.findOne({ where: { email: email.toLowerCase() } });
    if (existingUser) {
      throw new Error('Email is already registered!');
    }

    // Create user
    const user = await User.create({
      email: email.toLowerCase().trim(),
      password,
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      roles: ['ROLE_USER'],
      isAdmin: false
    });

    return user;
  }

  // Find user by email
  async findByEmail(email) {
    return await User.findOne({ 
      where: { email: email.toLowerCase() } 
    });
  }

  // Find user by ID
  async findById(id) {
    return await User.findByPk(id, {
      attributes: { exclude: ['password', 'resetToken', 'resetTokenExpiry'] }
    });
  }

  // Get all users
  async getAllUsers() {
    return await User.findAll({
      attributes: { exclude: ['password', 'resetToken', 'resetTokenExpiry'] }
    });
  }

  // Update user role
  async updateUserRole(userId, isAdmin) {
    const user = await User.findByPk(userId);
    if (!user) {
      throw new Error('User not found');
    }

    user.isAdmin = isAdmin;
    const roles = isAdmin ? ['ROLE_USER', 'ROLE_ADMIN'] : ['ROLE_USER'];
    user.roles = roles;
    
    await user.save();
    return user;
  }

  // Delete user
  async deleteUser(userId) {
    const user = await User.findByPk(userId);
    if (!user) {
      throw new Error('User not found');
    }
    await user.destroy();
  }

  // Get total users count
  async getTotalUsers() {
    return await User.count();
  }

  // Get admin users count
  async getAdminUsersCount() {
    return await User.count({ where: { isAdmin: true } });
  }

  // Create password reset token
  async createPasswordResetToken(email) {
    const user = await this.findByEmail(email);
    if (!user) {
      throw new Error('User not found');
    }

    const resetToken = uuidv4();
    const expirationMinutes = parseInt(process.env.PASSWORD_RESET_EXPIRATION_MINUTES) || 30;
    const resetTokenExpiry = new Date(Date.now() + expirationMinutes * 60 * 1000);

    user.resetToken = resetToken;
    user.resetTokenExpiry = resetTokenExpiry;
    await user.save();

    // Send reset email
    await emailService.sendPasswordResetEmail(user.email, resetToken, user.firstName);

    return resetToken;
  }

  // Validate reset token
  async validateResetToken(resetToken) {
    const user = await User.findOne({ where: { resetToken } });
    if (!user) {
      return false;
    }

    if (!user.resetTokenExpiry || new Date() > new Date(user.resetTokenExpiry)) {
      return false;
    }

    return true;
  }

  // Reset password
  async resetPassword(resetToken, newPassword) {
    const user = await User.findOne({ where: { resetToken } });
    if (!user) {
      throw new Error('Invalid reset token');
    }

    if (!user.resetTokenExpiry || new Date() > new Date(user.resetTokenExpiry)) {
      throw new Error('Reset token has expired');
    }

    user.password = newPassword;
    user.resetToken = null;
    user.resetTokenExpiry = null;
    await user.save();

    return true;
  }
}

module.exports = new UserService();
