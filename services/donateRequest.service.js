const { DonateRequest, DonateItem, User } = require('../models');

class DonateRequestService {
  // Create donation request
  async createRequest(requestData, userId) {
    try {
      // Check if donation item exists and is approved
      const donateItem = await DonateItem.findById(requestData.donationId);
      if (!donateItem) {
        throw new Error('Donation item not found');
      }

      if (donateItem.status !== 'APPROVED') {
        throw new Error('This donation is not available for requests');
      }

      // Check if user already has a pending/approved request for this donation
      const existingRequest = await DonateRequest.findOne({
        donationId: requestData.donationId,
        receiverUserId: userId,
        status: { $in: ['PENDING', 'APPROVED'] }
      });

      if (existingRequest) {
        throw new Error('You already have a request for this donation');
      }

      const request = new DonateRequest({
        donationId: requestData.donationId,
        receiverUserId: userId,
        description: requestData.description || '',
        quantity: requestData.quantity || 1,
        status: 'PENDING'
      });

      await request.save();
      return request;
    } catch (error) {
      console.error('Error creating donation request:', error);
      throw error;
    }
  }

  // Get user's donation requests
  async getMyRequests(userId) {
    try {
      const requests = await DonateRequest.find({ receiverUserId: userId })
        .populate({ path: 'donationId', populate: { path: 'userId', select: 'firstName lastName email' } })
        .populate('receiverUserId', 'firstName lastName email')
        .sort({ createdAt: -1 });
      
      return requests;
    } catch (error) {
      console.error('Error fetching user requests:', error);
      throw new Error('Failed to fetch donation requests');
    }
  }

  // Get pending requests (for admin)
  async getPendingRequests() {
    return await DonateRequest.find({ status: 'PENDING' })
      .populate({ path: 'donationId', populate: { path: 'userId', select: 'firstName lastName email' } })
      .populate('receiverUserId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Get all requests (for admin)
  async getAllRequests(status = null) {
    const filter = status ? { status } : {};

    return await DonateRequest.find(filter)
      .populate({ path: 'donationId', populate: { path: 'userId', select: 'firstName lastName email' } })
      .populate('receiverUserId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Approve request
  async approveRequest(id) {
    const request = await DonateRequest.findById(id);
    if (!request) {
      throw new Error('Request not found');
    }

    request.status = 'APPROVED';
    await request.save();

    // Update donation status to CLAIMED
    const donateItem = await DonateItem.findById(request.donationId);
    if (donateItem) {
      donateItem.status = 'CLAIMED';
      await donateItem.save();
    }

    return request;
  }

  // Reject request
  async rejectRequest(id, reason) {
    const request = await DonateRequest.findById(id);
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
    return await DonateRequest.countDocuments({ status });
  }
}

module.exports = new DonateRequestService();
