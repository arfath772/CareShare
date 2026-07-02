const { User } = require('../models');
const { v4: uuidv4 } = require('uuid');
const emailService = require('./email.service');

class UserService {
  // Register new user
  async registerUser(email, password, firstName, lastName, options = {}) {
    const {
      accountType = 'USER',
      ngoLegalName,
      ngoDarpanId,
      ngoPan,
      ngoOfficeAddress,
      ngoDocuments = {},
      hasFcra = false,
      phone,
      phoneNumber
    } = options;

    // Validate input
    if (!email || !password || !firstName || !lastName) {
      throw new Error('All fields are required');
    }

    const normalizedAccountType = String(accountType || 'USER').toUpperCase();

    if (!['USER', 'NGO'].includes(normalizedAccountType)) {
      throw new Error('Invalid account type');
    }

    if (normalizedAccountType === 'NGO') {
      if (!ngoLegalName || !ngoDarpanId || !ngoPan || !ngoOfficeAddress) {
        throw new Error('NGO legal details are required');
      }

      const requiredNgoDocs = [
        'registrationCertificate',
        'trustDeedOrMoa',
        'ngoPanCard',
        'nitiDarpanRegistration',
        'certificate12A',
        'certificate80G',
        'csr1Registration',
        'bankAccountProof',
        'auditedAccounts',
        'annualReport',
        'projectProposal',
        'kycMembers',
        'officeAddressProof',
        'governingBodyList'
      ];

      const missingDocs = requiredNgoDocs.filter((docKey) => !ngoDocuments[docKey]);
      if (missingDocs.length > 0) {
        throw new Error('All mandatory NGO documents must be uploaded');
      }

      if (hasFcra && !ngoDocuments.fcraCertificate) {
        throw new Error('FCRA certificate is required when foreign contribution is enabled');
      }
    }

    // Check if user already exists
    const existingUser = await User.findOne({ email: email.toLowerCase() });
    if (existingUser) {
      throw new Error('Email is already registered!');
    }

    // Create user
    const user = new User({
      email: email.toLowerCase().trim(),
      password,
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      phone: phone || phoneNumber,
      phoneNumber: phoneNumber || phone,
      roles: ['ROLE_USER'],
      isAdmin: false,
      accountType: normalizedAccountType,
      ngoStatus: normalizedAccountType === 'NGO' ? 'PENDING' : 'NOT_REQUIRED',
      ngoLegalName: normalizedAccountType === 'NGO' ? ngoLegalName.trim() : null,
      ngoDarpanId: normalizedAccountType === 'NGO' ? ngoDarpanId.trim() : null,
      ngoPan: normalizedAccountType === 'NGO' ? ngoPan.trim() : null,
      ngoOfficeAddress: normalizedAccountType === 'NGO' ? ngoOfficeAddress.trim() : null,
      ngoDocuments: normalizedAccountType === 'NGO' ? ngoDocuments : {}
    });

    await user.save();

    return user;
  }

  // Find user by email
  async findByEmail(email) {
    return await User.findOne({ email: email.toLowerCase().trim() });
  }

  // Find user by ID
  async findById(id) {
    const user = await User.findById(id).select('-password -resetToken -resetTokenExpiry');
    return user ? user.toJSON() : null;
  }

  // Get all users
  async getAllUsers() {
    const users = await User.find().select('-password -resetToken -resetTokenExpiry');
    return users.map(u => u.toJSON());
  }

  // Update user role
  async updateUserRole(userId, isAdmin) {
    const user = await User.findById(userId);
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
    const user = await User.findById(userId);
    if (!user) {
      throw new Error('User not found');
    }
    await user.remove();
  }

  // Get total users count
  async getTotalUsers() {
    return await User.countDocuments();
  }

  // Get admin users count
  async getAdminUsersCount() {
    return await User.countDocuments({ isAdmin: true });
  }

  async getPendingNgoUsers() {
    const users = await User.find({ accountType: 'NGO', ngoStatus: 'PENDING' })
      .select('-password -resetToken -resetTokenExpiry')
      .sort({ createdAt: -1 });
    return users.map(u => u.toJSON());
  }

  async getAllNgoUsers() {
    const users = await User.find({ accountType: 'NGO' })
      .select('-password -resetToken -resetTokenExpiry')
      .sort({ createdAt: -1 });
    return users.map(u => u.toJSON());
  }

  async approveNgoUser(userId) {
    const user = await User.findById(userId);
    if (!user || user.accountType !== 'NGO') {
      throw new Error('NGO user not found');
    }

    user.ngoStatus = 'APPROVED';
    user.ngoRejectionReason = null;
    await user.save();

    return user.toJSON();
  }

  async rejectNgoUser(userId, reason) {
    const user = await User.findById(userId);
    if (!user || user.accountType !== 'NGO') {
      throw new Error('NGO user not found');
    }

    user.ngoStatus = 'REJECTED';
    user.ngoRejectionReason = reason || 'NGO verification failed';
    await user.save();

    return user.toJSON();
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
    const user = await User.findOne({ resetToken });
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
    const user = await User.findOne({ resetToken });
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
