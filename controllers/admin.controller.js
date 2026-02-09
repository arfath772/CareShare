const userService = require('../services/user.service');
const productService = require('../services/product.service');
const exchangeRequestService = require('../services/exchangeRequest.service');
const donateItemService = require('../services/donateItem.service');
const donateRequestService = require('../services/donateRequest.service');
const emailService = require('../services/email.service');

class AdminController {
  // Get all users
  async getAllUsers(req, res) {
    try {
      const users = await userService.getAllUsers();
      return res.json(users);
    } catch (error) {
      console.error('Get all users error:', error);
      return res.status(400).json({ message: 'Error fetching users: ' + error.message });
    }
  }

  // Get admin stats
  async getAdminStats(req, res) {
    try {
      const stats = {
        totalUsers: await userService.getTotalUsers(),
        adminUsers: await userService.getAdminUsersCount(),
        regularUsers: await userService.getTotalUsers() - await userService.getAdminUsersCount(),
        pendingExchanges: await exchangeRequestService.getExchangeRequestCount('PENDING'),
        approvedExchanges: await exchangeRequestService.getExchangeRequestCount('APPROVED'),
        rejectedExchanges: await exchangeRequestService.getExchangeRequestCount('REJECTED')
      };

      return res.json(stats);
    } catch (error) {
      console.error('Get admin stats error:', error);
      return res.status(400).json({ message: 'Error fetching stats: ' + error.message });
    }
  }

  // Update user role
  async updateUserRole(req, res) {
    try {
      const { userId } = req.params;
      const { isAdmin } = req.body;

      if (isAdmin === undefined) {
        throw new Error('isAdmin field is required');
      }

      const updatedUser = await userService.updateUserRole(userId, isAdmin);

      return res.json({
        message: 'User role updated successfully',
        user: updatedUser
      });
    } catch (error) {
      console.error('Update user role error:', error);
      return res.status(400).json({ message: 'Error updating user role: ' + error.message });
    }
  }

  // Delete user
  async deleteUser(req, res) {
    try {
      const { userId } = req.params;
      await userService.deleteUser(userId);

      return res.json({ message: 'User deleted successfully' });
    } catch (error) {
      console.error('Delete user error:', error);
      return res.status(400).json({ message: 'Error deleting user: ' + error.message });
    }
  }

  // Get all pending products
  async getPendingProducts(req, res) {
    try {
      const products = await productService.getPendingProducts();
      return res.json(products);
    } catch (error) {
      console.error('Get pending products error:', error);
      return res.status(400).json({ message: 'Error fetching pending products: ' + error.message });
    }
  }

  // Approve product
  async approveProduct(req, res) {
    try {
      const { productId } = req.params;
      const approvedProduct = await productService.approveProduct(productId);

      return res.json({
        message: 'Product approved successfully',
        product: approvedProduct
      });
    } catch (error) {
      console.error('Approve product error:', error);
      return res.status(400).json({ message: 'Error approving product: ' + error.message });
    }
  }

  // Reject product
  async rejectProduct(req, res) {
    try {
      const { productId } = req.params;
      const { rejectionReason } = req.body;

      const rejectedProduct = await productService.rejectProduct(productId, rejectionReason);

      return res.json({
        message: 'Product rejected successfully',
        product: rejectedProduct
      });
    } catch (error) {
      console.error('Reject product error:', error);
      return res.status(400).json({ message: 'Error rejecting product: ' + error.message });
    }
  }

  // Get product stats
  async getProductStats(req, res) {
    try {
      const stats = {
        pendingProducts: await productService.getPendingProductsCount(),
        approvedProducts: await productService.getApprovedProductsCount()
      };

      return res.json(stats);
    } catch (error) {
      console.error('Get product stats error:', error);
      return res.status(400).json({ message: 'Error fetching product stats: ' + error.message });
    }
  }

  // Get pending donation items
  async getPendingDonatedItems(req, res) {
    try {
      const items = await donateItemService.getPendingDonations();
      return res.json(items);
    } catch (error) {
      console.error('Get pending donations error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get all donation items
  async getAllDonatedItems(req, res) {
    try {
      const items = await donateItemService.getAllDonations();
      return res.json(items);
    } catch (error) {
      console.error('Get all donations error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Approve donation item
  async approveDonatedItem(req, res) {
    try {
      const { id } = req.params;
      const donation = await donateItemService.approveDonation(id);
      return res.json(donation);
    } catch (error) {
      console.error('Approve donation error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Reject donation item
  async rejectDonatedItem(req, res) {
    try {
      const { id } = req.params;
      const { reason } = req.body;
      const donation = await donateItemService.rejectDonation(id, reason);
      return res.json(donation);
    } catch (error) {
      console.error('Reject donation error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get pending donation requests
  async getPendingDonationRequests(req, res) {
    try {
      const requests = await donateRequestService.getPendingRequests();
      return res.json(requests);
    } catch (error) {
      console.error('Get pending donation requests error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get all donation requests
  async getAllDonationRequests(req, res) {
    try {
      const requests = await donateRequestService.getAllRequests(null);
      return res.json(requests);
    } catch (error) {
      console.error('Get all donation requests error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Approve donation request
  async approveDonationRequest(req, res) {
    try {
      const { id } = req.params;
      const request = await donateRequestService.approveRequest(id);
      return res.json(request);
    } catch (error) {
      console.error('Approve donation request error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Reject donation request
  async rejectDonationRequest(req, res) {
    try {
      const { id } = req.params;
      const { reason } = req.body;
      const request = await donateRequestService.rejectRequest(id, reason);
      return res.json(request);
    } catch (error) {
      console.error('Reject donation request error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get donation stats
  async getDonationStats(req, res) {
    try {
      const stats = {
        pendingItems: await donateItemService.getCountByStatus('PENDING'),
        approvedItems: await donateItemService.getCountByStatus('APPROVED'),
        claimedItems: await donateItemService.getCountByStatus('CLAIMED'),
        pendingRequests: await donateRequestService.getRequestCountByStatus('PENDING'),
        approvedRequests: await donateRequestService.getRequestCountByStatus('APPROVED'),
        rejectedRequests: await donateRequestService.getRequestCountByStatus('REJECTED')
      };

      return res.json(stats);
    } catch (error) {
      console.error('Get donation stats error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get all exchange requests
  async getExchangeRequests(req, res) {
    try {
      const { status } = req.query;
      const requests = await exchangeRequestService.getAllExchangeRequests(status);
      return res.json(requests);
    } catch (error) {
      console.error('Get exchange requests error:', error);
      return res.status(400).json({ message: 'Error fetching exchange requests: ' + error.message });
    }
  }

  // Get exchange request stats
  async getExchangeRequestStats(req, res) {
    try {
      const stats = {
        pending: await exchangeRequestService.getExchangeRequestCount('PENDING'),
        approved: await exchangeRequestService.getExchangeRequestCount('APPROVED'),
        rejected: await exchangeRequestService.getExchangeRequestCount('REJECTED'),
        total: await exchangeRequestService.getExchangeRequestCount(null)
      };

      return res.json(stats);
    } catch (error) {
      console.error('Get exchange request stats error:', error);
      return res.status(400).json({ message: 'Error fetching exchange request stats: ' + error.message });
    }
  }

  // Approve exchange request
  async approveExchangeRequest(req, res) {
    try {
      const { id } = req.params;
      const approvedRequest = await exchangeRequestService.approveExchangeRequest(id);

      // Send notification
      const ownerEmail = approvedRequest.targetProduct.user.email;
      const requesterEmail = approvedRequest.requester.email;

      try {
        await emailService.sendExchangeStatusUpdate(approvedRequest, ownerEmail, requesterEmail);
      } catch (emailError) {
        console.error('Error sending notification:', emailError);
      }

      return res.json({
        message: 'Exchange request approved successfully',
        exchangeRequest: approvedRequest
      });
    } catch (error) {
      console.error('Approve exchange request error:', error);
      return res.status(400).json({ message: 'Error approving exchange request: ' + error.message });
    }
  }

  // Reject exchange request
  async rejectExchangeRequest(req, res) {
    try {
      const { id } = req.params;
      const { rejectionReason } = req.body;

      if (!rejectionReason || rejectionReason.trim() === '') {
        throw new Error('Rejection reason is required');
      }

      const rejectedRequest = await exchangeRequestService.rejectExchangeRequest(id, rejectionReason);

      // Send notification
      const ownerEmail = rejectedRequest.targetProduct.user.email;
      const requesterEmail = rejectedRequest.requester.email;

      try {
        await emailService.sendExchangeStatusUpdate(rejectedRequest, ownerEmail, requesterEmail);
      } catch (emailError) {
        console.error('Error sending notification:', emailError);
      }

      return res.json({
        message: 'Exchange request rejected successfully',
        exchangeRequest: rejectedRequest
      });
    } catch (error) {
      console.error('Reject exchange request error:', error);
      return res.status(400).json({ message: 'Error rejecting exchange request: ' + error.message });
    }
  }

  // Delete exchange request
  async deleteExchangeRequest(req, res) {
    try {
      const { id } = req.params;
      await exchangeRequestService.deleteExchangeRequest(id);

      return res.json({ message: 'Exchange request deleted successfully' });
    } catch (error) {
      console.error('Delete exchange request error:', error);
      return res.status(400).json({ message: 'Error deleting exchange request: ' + error.message });
    }
  }
}

module.exports = new AdminController();
