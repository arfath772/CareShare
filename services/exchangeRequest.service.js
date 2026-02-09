const { ExchangeRequest, Product, User } = require('../models');

class ExchangeRequestService {
  // Submit exchange request
  async submitExchangeRequest(requestData, files, userId) {
    try {
      // Check if target product exists
      const targetProduct = await Product.findByPk(requestData.targetProductId);
      if (!targetProduct) {
        throw new Error('Target product not found');
      }

      if (targetProduct.status !== 'APPROVED') {
        throw new Error('This product is not available for exchange');
      }

      // Prepare image paths
      const imagePaths = files && files.length > 0 
        ? files.map(file => `/uploads/exchange-items/${file.filename}`)
        : [];

      const exchangeRequest = await ExchangeRequest.create({
        targetProductId: requestData.targetProductId,
        exchangeItemName: requestData.itemName,
        exchangeItemCategory: requestData.category,
        exchangeItemDescription: requestData.description,
        exchangeItemImages: imagePaths,
        additionalMessage: requestData.additionalMessage || '',
        requesterId: userId,
        status: 'PENDING',
        createdAt: new Date()
      });

      // Load relationships
      await exchangeRequest.reload({
        include: [
          {
            model: Product,
            as: 'targetProduct',
            include: [{
              model: User,
              as: 'user',
              attributes: ['id', 'firstName', 'lastName', 'email']
            }]
          },
          {
            model: User,
            as: 'requester',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }
        ]
      });

      return exchangeRequest;
    } catch (error) {
      console.error('Error submitting exchange request:', error);
      throw error;
    }
  }

  // Get user's exchange requests
  async getUserExchangeRequests(userId, status = null) {
    const where = { requesterId: userId };
    if (status) where.status = status;

    return await ExchangeRequest.findAll({
      where,
      include: [
        {
          model: Product,
          as: 'targetProduct',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'requester',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get received exchange requests (for product owners)
  async getReceivedExchangeRequests(userId, status = null) {
    const where = status ? { status } : {};

    return await ExchangeRequest.findAll({
      where,
      include: [
        {
          model: Product,
          as: 'targetProduct',
          where: { userId },
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'requester',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['createdAt', 'DESC']]
    });
  }

  // Get all exchange requests (admin)
  async getAllExchangeRequests(status = null) {
    const where = status ? { status } : {};

    return await ExchangeRequest.findAll({
      where,
      include: [
        {
          model: Product,
          as: 'targetProduct',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'requester',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ],
      order: [['createdAt', 'DESC']]
    });
  }

  // Find exchange request by ID
  async findById(id) {
    return await ExchangeRequest.findByPk(id, {
      include: [
        {
          model: Product,
          as: 'targetProduct',
          include: [{
            model: User,
            as: 'user',
            attributes: ['id', 'firstName', 'lastName', 'email']
          }]
        },
        {
          model: User,
          as: 'requester',
          attributes: ['id', 'firstName', 'lastName', 'email']
        }
      ]
    });
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
    const request = await ExchangeRequest.findByPk(id);
    if (!request) {
      throw new Error('Exchange request not found');
    }

    await request.destroy();
  }

  // Save exchange request
  async save(exchangeRequest) {
    return await exchangeRequest.save();
  }

  // Get exchange request count by status
  async getExchangeRequestCount(status = null) {
    const where = status ? { status } : {};
    return await ExchangeRequest.count({ where });
  }
}

module.exports = new ExchangeRequestService();
