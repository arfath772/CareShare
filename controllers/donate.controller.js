const donateItemService = require('../services/donateItem.service');
const donateRequestService = require('../services/donateRequest.service');

class DonateController {
  // Add donation
  async addDonation(req, res) {
    try {
      const donationData = {
        itemType: req.body.itemType,
        itemName: req.body.itemName,
        quantity: parseInt(req.body.quantity),
        itemCondition: req.body.itemCondition,
        pickupAddress: req.body.pickupAddress
      };

      const files = req.files;

      const donation = await donateItemService.addDonation(donationData, files, req.user.id);

      return res.json(donation);
    } catch (error) {
      console.error('Add donation error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get available donations
  async getAvailableDonations(req, res) {
    try {
      const { type } = req.query;
      const donations = await donateItemService.getAvailableDonations(type);
      return res.json(donations);
    } catch (error) {
      console.error('Get available donations error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get my donations
  async getMyDonations(req, res) {
    try {
      const donations = await donateItemService.getMyDonations(req.user.id);
      return res.json(donations);
    } catch (error) {
      console.error('Get my donations error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get donation by ID
  async getDonationById(req, res) {
    try {
      const { id } = req.params;
      const donation = await donateItemService.getDonationById(id);

      if (!donation) {
        return res.status(404).json({ message: 'Donation not found' });
      }

      return res.json(donation);
    } catch (error) {
      console.error('Get donation error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Delete donation
  async deleteDonation(req, res) {
    try {
      const { id } = req.params;
      await donateItemService.deleteDonation(id, req.user.id);
      return res.json({ message: 'Donation deleted successfully' });
    } catch (error) {
      console.error('Delete donation error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Create donation request
  async createRequest(req, res) {
    try {
      const requestData = {
        donationId: req.body.donationId,
        description: req.body.description
      };

      const request = await donateRequestService.createRequest(requestData, req.user.id);
      return res.json(request);
    } catch (error) {
      console.error('Create request error:', error);
      return res.status(400).json({ message: error.message });
    }
  }

  // Get my donation requests
  async getMyRequests(req, res) {
    try {
      const requests = await donateRequestService.getMyRequests(req.user.id);
      return res.json(requests);
    } catch (error) {
      console.error('Get my requests error:', error);
      return res.status(400).json({ message: error.message });
    }
  }
}

module.exports = new DonateController();
