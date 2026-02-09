const exchangeRequestService = require('../services/exchangeRequest.service');
const userService = require('../services/user.service');
const emailService = require('../services/email.service');

class ExchangeRequestController {
  // Submit exchange request
  async submitExchangeRequest(req, res) {
    try {
      const { targetProductId, itemName, category, description, additionalMessage } = req.body;
      const files = req.files;

      // Validation
      if (!targetProductId || !itemName || !category || !description) {
        return res.status(400).json({ error: 'All required fields must be provided' });
      }

      if (!files || files.length === 0) {
        return res.status(400).json({ error: 'At least one image is required' });
      }

      if (files.length > 12) {
        return res.status(400).json({ error: 'Maximum 12 images allowed' });
      }

      const requestData = {
        targetProductId,
        itemName,
        category,
        description,
        additionalMessage
      };

      const exchangeRequest = await exchangeRequestService.submitExchangeRequest(
        requestData,
        files,
        req.user.id
      );

      // Send notifications
      const ownerEmail = exchangeRequest.targetProduct.user.email;
      const requesterEmail = req.user.email;

      try {
        await emailService.sendExchangeRequestNotifications(exchangeRequest, ownerEmail, requesterEmail);
      } catch (emailError) {
        console.error('Error sending notifications:', emailError);
      }

      return res.json({
        success: true,
        message: 'Exchange request submitted successfully',
        exchangeRequest,
        imageCount: files.length
      });
    } catch (error) {
      console.error('Submit exchange request error:', error);
      return res.status(400).json({ error: 'Error submitting exchange request: ' + error.message });
    }
  }

  // Get my exchange requests
  async getMyExchangeRequests(req, res) {
    try {
      const { status } = req.query;
      const requests = await exchangeRequestService.getUserExchangeRequests(req.user.id, status);
      return res.json(requests);
    } catch (error) {
      console.error('Get my exchange requests error:', error);
      return res.status(400).json({ error: 'Error fetching exchange requests: ' + error.message });
    }
  }

  // Get received exchange requests
  async getReceivedExchangeRequests(req, res) {
    try {
      const { status } = req.query;
      const requests = await exchangeRequestService.getReceivedExchangeRequests(req.user.id, status);
      return res.json(requests);
    } catch (error) {
      console.error('Get received requests error:', error);
      return res.status(400).json({ error: 'Error fetching received exchange requests: ' + error.message });
    }
  }

  // Accept exchange request
  async acceptExchangeRequest(req, res) {
    try {
      const { id } = req.params;

      const exchangeRequest = await exchangeRequestService.findById(id);
      if (!exchangeRequest) {
        return res.status(404).json({ error: 'Exchange request not found' });
      }

      // Verify current user is the owner
      if (exchangeRequest.targetProduct.userId !== req.user.id) {
        return res.status(403).json({ error: 'You are not authorized to accept this exchange request' });
      }

      exchangeRequest.status = 'APPROVED';
      const updatedRequest = await exchangeRequestService.save(exchangeRequest);

      // Send notification
      const ownerEmail = exchangeRequest.targetProduct.user.email;
      const requesterEmail = exchangeRequest.requester.email;

      try {
        await emailService.sendExchangeStatusUpdate(updatedRequest, ownerEmail, requesterEmail);
      } catch (emailError) {
        console.error('Error sending notification:', emailError);
      }

      return res.json({
        success: true,
        message: 'Exchange request accepted successfully',
        exchangeRequest: updatedRequest
      });
    } catch (error) {
      console.error('Accept exchange request error:', error);
      return res.status(400).json({ error: 'Error accepting exchange request: ' + error.message });
    }
  }

  // Decline exchange request
  async declineExchangeRequest(req, res) {
    try {
      const { id } = req.params;
      const { rejectionReason } = req.body;

      const exchangeRequest = await exchangeRequestService.findById(id);
      if (!exchangeRequest) {
        return res.status(404).json({ error: 'Exchange request not found' });
      }

      // Verify current user is the owner
      if (exchangeRequest.targetProduct.userId !== req.user.id) {
        return res.status(403).json({ error: 'You are not authorized to decline this exchange request' });
      }

      exchangeRequest.status = 'REJECTED';
      exchangeRequest.rejectionReason = rejectionReason || 'No reason provided';
      const updatedRequest = await exchangeRequestService.save(exchangeRequest);

      // Send notification
      const ownerEmail = exchangeRequest.targetProduct.user.email;
      const requesterEmail = exchangeRequest.requester.email;

      try {
        await emailService.sendExchangeStatusUpdate(updatedRequest, ownerEmail, requesterEmail);
      } catch (emailError) {
        console.error('Error sending notification:', emailError);
      }

      return res.json({
        success: true,
        message: 'Exchange request declined successfully',
        exchangeRequest: updatedRequest
      });
    } catch (error) {
      console.error('Decline exchange request error:', error);
      return res.status(400).json({ error: 'Error declining exchange request: ' + error.message });
    }
  }

  // Cancel exchange request
  async cancelExchangeRequest(req, res) {
    try {
      const { id } = req.params;

      const exchangeRequest = await exchangeRequestService.findById(id);
      if (!exchangeRequest) {
        return res.status(404).json({ error: 'Exchange request not found' });
      }

      // Verify current user is the requester
      if (exchangeRequest.requesterId !== req.user.id) {
        return res.status(403).json({ error: 'You are not authorized to cancel this exchange request' });
      }

      if (exchangeRequest.status !== 'PENDING') {
        return res.status(400).json({ error: 'Only pending requests can be cancelled' });
      }

      await exchangeRequestService.deleteExchangeRequest(id);

      return res.json({
        success: true,
        message: 'Exchange request cancelled successfully'
      });
    } catch (error) {
      console.error('Cancel exchange request error:', error);
      return res.status(400).json({ error: 'Error cancelling exchange request: ' + error.message });
    }
  }

  // Get exchange request by ID
  async getExchangeRequestById(req, res) {
    try {
      const { id } = req.params;
      const exchangeRequest = await exchangeRequestService.findById(id);

      if (!exchangeRequest) {
        return res.status(404).json({ error: 'Exchange request not found' });
      }

      return res.json(exchangeRequest);
    } catch (error) {
      console.error('Get exchange request error:', error);
      return res.status(400).json({ error: 'Error fetching exchange request: ' + error.message });
    }
  }
}

module.exports = new ExchangeRequestController();
