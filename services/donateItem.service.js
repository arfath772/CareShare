const { DonateItem, DonateRequest, User } = require('../models');
const { Op } = require('sequelize');

class DonateItemService {
  // Add donation
  async addDonation(donationData, files, userId) {
    try {
      // Prepare image URLs
      let imageUrls = [];
      let mainImageUrl = null;

      if (files && files.length > 0) {
        imageUrls = files.map(file => `/uploads/donations/${file.filename}`);
        mainImageUrl = imageUrls[0];
      }

      const donation = await DonateItem.create({
        itemType: donationData.itemType,
        itemName: donationData.itemName,
        quantity: donationData.quantity,
        itemCondition: donationData.itemCondition,
        pickupAddress: donationData.pickupAddress,
        imageUrls: JSON.stringify(imageUrls),
        mainImageUrl: mainImageUrl,
        status: 'PENDING',
        userId: userId,
        createdAt: new Date(),
        updatedAt: new Date()
      });

      return donation;
    } catch (error) {
      console.error('Error adding donation:', error);
      throw new Error('Failed to add donation: ' + error.message);
    }
  }

  // Get available donations (approved and not claimed)
  async getAvailableDonations(type = null) {
    const where = { status: 'APPROVED' };
    if (type && type !== 'all') where.itemType = type;

    return await DonateItem.findAll({
      where,
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get user's donations
  async getMyDonations(userId) {
    return await DonateItem.findAll({
      where: { userId },
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get donation by ID
  async getDonationById(id) {
    const donation = await DonateItem.findByPk(id, {
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }]
    });

    if (donation && donation.imageUrls) {
      try {
        donation.imageUrls = JSON.parse(donation.imageUrls);
      } catch (e) {
        donation.imageUrls = [];
      }
    }

    return donation;
  }

  // Delete donation
  async deleteDonation(id, userId) {
    const donation = await DonateItem.findByPk(id);
    if (!donation) {
      throw new Error('Donation not found');
    }

    if (donation.userId !== userId) {
      throw new Error('You are not authorized to delete this donation');
    }

    await donation.destroy();
  }

  // Get pending donations
  async getPendingDonations() {
    return await DonateItem.findAll({
      where: { status: 'PENDING' },
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get all donations
  async getAllDonations() {
    return await DonateItem.findAll({
      include: [{
        model: User,
        as: 'user',
        attributes: ['id', 'firstName', 'lastName', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });
  }

  // Approve donation
  async approveDonation(id) {
    const donation = await DonateItem.findByPk(id);
    if (!donation) {
      throw new Error('Donation not found');
    }

    donation.status = 'APPROVED';
    donation.updatedAt = new Date();
    await donation.save();

    return donation;
  }

  // Reject donation
  async rejectDonation(id, reason) {
    const donation = await DonateItem.findByPk(id);
    if (!donation) {
      throw new Error('Donation not found');
    }

    donation.status = 'REJECTED';
    donation.rejectionReason = reason;
    donation.updatedAt = new Date();
    await donation.save();

    return donation;
  }

  // Get donations count by status
  async getCountByStatus(status) {
    return await DonateItem.count({ where: { status } });
  }
}

module.exports = new DonateItemService();
