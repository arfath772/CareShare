const purchaseService = require('../services/purchase.service');

class PurchaseController {
  // Create purchase
  async createPurchase(req, res) {
    try {
      const purchaseData = {
        productId: req.body.productId,
        fullName: req.body.fullName,
        email: req.body.email,
        phone: req.body.phone,
        shippingAddress: req.body.shippingAddress,
        paymentMethod: req.body.paymentMethod
      };

      const response = await purchaseService.createPurchase(purchaseData, req.user.email);

      return res.json({
        success: true,
        message: response.message,
        purchase: response.purchase
      });
    } catch (error) {
      console.error('Create purchase error:', error);
      return res.status(400).json({
        success: false,
        message: error.message
      });
    }
  }

  // Get my purchases
  async getMyPurchases(req, res) {
    try {
      const purchases = await purchaseService.getPurchasesByBuyer(req.user.email);

      return res.json({
        success: true,
        purchases
      });
    } catch (error) {
      console.error('Get my purchases error:', error);
      return res.status(400).json({
        success: false,
        message: error.message
      });
    }
  }

  // Get my sales
  async getMySales(req, res) {
    try {
      const sales = await purchaseService.getSalesBySeller(req.user.email);

      return res.json({
        success: true,
        sales
      });
    } catch (error) {
      console.error('Get my sales error:', error);
      return res.status(400).json({
        success: false,
        message: error.message
      });
    }
  }

  // Update purchase status
  async updatePurchaseStatus(req, res) {
    try {
      const { purchaseId } = req.params;
      const { status } = req.query;

      if (!status) {
        return res.status(400).json({
          success: false,
          message: 'Status parameter is required'
        });
      }

      const validStatuses = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
      if (!validStatuses.includes(status.toUpperCase())) {
        return res.status(400).json({
          success: false,
          message: 'Invalid status value'
        });
      }

      const updatedPurchase = await purchaseService.updatePurchaseStatus(
        purchaseId,
        status.toUpperCase(),
        req.user.email
      );

      return res.json({
        success: true,
        message: 'Purchase status updated successfully',
        purchase: updatedPurchase
      });
    } catch (error) {
      console.error('Update purchase status error:', error);
      return res.status(400).json({
        success: false,
        message: error.message
      });
    }
  }
}

module.exports = new PurchaseController();
