const { DonateRequest, DonateItem, User } = require('../models');

class DonateRequestService {
  // Create donation request
  async createRequest(requestData, userId) {
    try {
      // Check if donation item exists and is approved
      const donateItem = await DonateItem.findByPk(requestData.donationId);
      if (!donateItem) {
        throw new Error('Donation item not found');
      }

      if (donateItem.status !== 'APPROVED') {
        throw new Error('This donation is not available for requests');
      }

      // Check if user already has a pending/approved request for this donation
      const existingRequest = await DonateRequest.findOne({
        where: {
          donationId: requestData.donationId,
          receiverUserId: userId,
          status: ['PENDING', 'APPROVED']
        }
      });

      if (existingRequest) {
        throw new Error('You already have a request for this donation');
      }

      const request = await DonateRequest.create({
        donationId: requestData.donationId,
        receiverUserId: userId,
        description: requestData.description || '',
        status: 'PENDING',
        requestedDate: new Date()
      });

      return request;
    } catch (error) {
      console.error('Error creating donation request:', error);
      throw error;
    }
  }

  // Get user's donation requests
  async getMyRequests(userId) {
    return await DonateRequest.findAll({
      where: { receiverUserId: userId },
      include: [
        {
          model: DonateItem,
          as: 'donateItem',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'receiver',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['requestedDate', 'DESC']]
    });
  }

  // Get pending requests (for admin)
  async getPendingRequests() {
    return await DonateRequest.findAll({
      where: { status: 'PENDING' },
      include: [
        {
          model: DonateItem,
          as: 'donateItem',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'receiver',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['requestedDate', 'DESC']]
    });
  }

  // Get all requests (for admin)
  async getAllRequests(status = null) {
    const where = status ? { status } : {};

    return await DonateRequest.findAll({
      where,
      include: [
        {
          model: DonateItem,
          as: 'donateItem',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'receiver',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['requestedDate', 'DESC']]
    });
  }

  // Approve request
  async approveRequest(id) {
    const request = await DonateRequest.findByPk(id);
    if (!request) {
      throw new Error('Request not found');
    }

    request.status = 'APPROVED';
    await request.save();

    // Update donation status to CLAIMED
    const donateItem = await DonateItem.findByPk(request.donationId);
    if (donateItem) {
      donateItem.status = 'CLAIMED';
      donateItem.updatedAt = new Date();
      await donateItem.save();
    }

    return request;
  }

  // Reject request
  async rejectRequest(id, reason) {
    const request = await DonateRequest.findByPk(id);
    if (!request) {
      throw new Error('Request not found');
    }

    request.status = 'REJECTED';
    request.rejectionReason = reason;
    await request.save();

    return request;
  }

  // Get request count by status
  async getRequestCountByStatus(status) {
    return await DonateRequest.count({ where: { status } });
  }
}

module.exports = new DonateRequestService();
