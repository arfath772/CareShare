const { ExchangeRequest, Product, User } = require('../models');

class ExchangeRequestService {
  // Submit exchange request
  async submitExchangeRequest(requestData, files, userId) {
    try {
      // Check if target product exists
      const targetProduct = await Product.findById(requestData.targetProductId);
      if (!targetProduct) {
        throw new Error('Target product not found');
      }

      if (targetProduct.status !== 'APPROVED') {
        throw new Error('This product is not available for exchange');
      }

      // Prepare image buffers
      const exchangeImages = Array.isArray(files)
        ? files
          .filter((file) => file && file.buffer)
          .map((file) => ({
            filename: file.originalname,
            contentType: file.mimetype,
            data: file.buffer
          }))
        : [];

      const exchangeRequest = new ExchangeRequest({
        targetProductId: requestData.targetProductId,
        exchangeItemName: requestData.itemName,
        exchangeItemCategory: requestData.category,
        exchangeItemDescription: requestData.description,
        exchangeItemImages: [], // Dynamically mapped via virtual transform
        exchangeImages: exchangeImages,
        additionalMessage: requestData.additionalMessage || '',
        requesterId: userId,
        status: 'PENDING'
      });

      await exchangeRequest.save();

      // Load relationships
      return await exchangeRequest.populate([
        { path: 'targetProductId', populate: { path: 'userId', select: 'firstName lastName email' } },
        { path: 'requesterId', select: 'firstName lastName email' }
      ]);
    } catch (error) {
      console.error('Error submitting exchange request:', error);
      throw error;
    }
  }

  // Get user's exchange requests
  async getUserExchangeRequests(userId, status = null) {
    const filter = { requesterId: userId };
    if (status) filter.status = status;

    return await ExchangeRequest.find(filter)
      .populate({ path: 'targetProductId', populate: { path: 'userId', select: 'firstName lastName email' } })
      .populate('requesterId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Get received exchange requests (for product owners)
  async getReceivedExchangeRequests(userId, status = null) {
    const exchangeRequests = await ExchangeRequest.find(status ? { status } : {})
      .populate({
        path: 'targetProductId',
        match: { userId },
        populate: { path: 'userId', select: 'firstName lastName email' }
      })
      .populate('requesterId', 'firstName lastName email')
      .sort({ createdAt: -1 });

    return exchangeRequests.filter(req => req.targetProductId);
  }

  // Get all exchange requests (admin)
  async getAllExchangeRequests(status = null) {
    const filter = status ? { status } : {};

    return await ExchangeRequest.find(filter)
      .populate({ path: 'targetProductId', populate: { path: 'userId', select: 'firstName lastName email' } })
      .populate('requesterId', 'firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Find exchange request by ID
  async findById(id) {
    return await ExchangeRequest.findById(id)
      .populate({ path: 'targetProductId', populate: { path: 'userId', select: 'firstName lastName email' } })
      .populate('requesterId', 'firstName lastName email');
  }

  // Approve exchange request (admin or owner)
  async approveExchangeRequest(id) {
    const request = await this.findById(id);
    if (!request) {
      throw new Error('Exchange request not found');
    }

    request.status = 'APPROVED';
    await request.save();

    return request;
  }

  // Reject exchange request (admin or owner)
  async rejectExchangeRequest(id, rejectionReason) {
    const request = await this.findById(id);
    if (!request) {
      throw new Error('Exchange request not found');
    }

    request.status = 'REJECTED';
    request.rejectionReason = rejectionReason;
    await request.save();

    return request;
  }

  // Delete exchange request
  async deleteExchangeRequest(id) {
    const request = await ExchangeRequest.findById(id);
    if (!request) {
      throw new Error('Exchange request not found');
    }

    await request.remove();
  }

  // Save exchange request
  async save(exchangeRequest) {
    return await exchangeRequest.save();
  }

  // Get exchange request count by status
  async getExchangeRequestCount(status = null) {
    const filter = status ? { status } : {};
    return await ExchangeRequest.countDocuments(filter);
  }

  // Get exchange request image by index
  async getExchangeRequestImage(requestId, imageIndex) {
    const request = await ExchangeRequest.findById(requestId).select('exchangeImages');
    if (!request) {
      throw new Error('Exchange request not found');
    }

    if (Array.isArray(request.exchangeImages) && request.exchangeImages.length > 0) {
      const index = Number.parseInt(imageIndex, 10);
      if (Number.isNaN(index) || index < 0 || index >= request.exchangeImages.length) {
        throw new Error('Image not found');
      }

      const img = request.exchangeImages[index];
      return {
        contentType: img.contentType || 'application/octet-stream',
        data: img.data
      };
    }

    throw new Error('Exchange request has no images');
  }
}

module.exports = new ExchangeRequestService();
