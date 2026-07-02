const { MoneyDonation } = require('../models');
const paymentUtils = require('../utils/payment-utils');

class MoneyDonationService {
  // Helper to convert Decimal128 amount to JS number on returned objects
  _serializeDonation(donation) {
    if (!donation) return donation;
    const obj = (typeof donation.toObject === 'function') ? donation.toObject() : { ...donation };
    
    if (obj._id) {
      obj.id = obj._id.toString();
    }

    try {
      if (obj.amount && obj.amount.toString) {
        obj.amount = parseFloat(obj.amount.toString());
      } else {
        obj.amount = parseFloat(obj.amount) || 0;
      }
    } catch (e) {
      obj.amount = 0;
    }
    return obj;
  }
  // Create a new money donation record
  async createDonation(donationData, userId = null, ipAddress = null) {
    try {
      const sanitizedData = paymentUtils.sanitizeDonorData(donationData);
      
      const donation = new MoneyDonation({
        donorName: sanitizedData.donorName,
        donorEmail: sanitizedData.donorEmail,
        donorPhone: sanitizedData.donorPhone,
        amount: paymentUtils.validateAmount(donationData.amount),
        currency: 'INR',
        paymentMethod: donationData.paymentMethod,
        transactionId: paymentUtils.generateTransactionId(),
        receiptNumber: paymentUtils.generateReceiptNumber(),
        message: sanitizedData.message,
        isAnonymous: sanitizedData.isAnonymous,
        anonymousDonorName: sanitizedData.anonymousDonorName,
        userId: userId,
        ipAddress: ipAddress,
        metadata: donationData.metadata || {}
      });

      await donation.save();
      return this._serializeDonation(donation);
    } catch (error) {
      throw new Error(`Failed to create donation: ${error.message}`);
    }
  }

  // Get donation by ID
  async getDonationById(donationId) {
    try {
      const donation = await MoneyDonation.findById(donationId).populate('userId');

      if (!donation) {
        throw new Error('Donation not found');
      }

      return this._serializeDonation(donation);
    } catch (error) {
      throw new Error(`Failed to fetch donation: ${error.message}`);
    }
  }

  // Get all donations by user
  async getUserDonations(userId, email = null) {
    try {
      // First, try to link any unlinked donations matching this email
      if (email && userId) {
        await MoneyDonation.updateMany(
          { userId: null, donorEmail: email },
          { userId: userId }
        );
      }

      const donations = await MoneyDonation.find({ userId }).sort({ createdAt: -1 });
      return donations.map(d => this._serializeDonation(d));
    } catch (error) {
      throw new Error(`Failed to fetch user donations: ${error.message}`);
    }
  }

  // Update donation payment status
  async updatePaymentStatus(donationId, status, paymentData) {
    try {
      const donation = await MoneyDonation.findById(donationId);

      if (!donation) {
        throw new Error('Donation not found');
      }

      donation.paymentStatus = status;
      donation.paymentId = paymentData.paymentId;
      donation.paymentSignature = paymentData.paymentSignature;
      donation.metadata = paymentData.metadata || donation.metadata;

      await donation.save();
      return this._serializeDonation(donation);
    } catch (error) {
      throw new Error(`Failed to update payment status: ${error.message}`);
    }
  }

  // Mark receipt as generated
  async markReceiptGenerated(donationId, receiptUrl) {
    try {
      const donation = await MoneyDonation.findById(donationId);

      if (!donation) {
        throw new Error('Donation not found');
      }

      donation.receiptDownloadUrl = receiptUrl;
      donation.receiptGeneratedAt = new Date();

      await donation.save();
      return this._serializeDonation(donation);
    } catch (error) {
      throw new Error(`Failed to mark receipt generated: ${error.message}`);
    }
  }

  // Get all donations (for admin)
  async getAllDonations(filters = {}) {
    try {
      const filter = {};

      if (filters.status) {
        filter.paymentStatus = filters.status;
      }

      if (filters.startDate || filters.endDate) {
        filter.createdAt = {};
        if (filters.startDate) {
          filter.createdAt.$gte = new Date(filters.startDate);
        }
        if (filters.endDate) {
          filter.createdAt.$lte = new Date(filters.endDate);
        }
      }

      const donations = await MoneyDonation.find(filter)
        .populate('userId')
        .sort({ createdAt: -1 })
        .limit(filters.limit || 100)
        .skip(filters.offset || 0);

      return donations.map(d => this._serializeDonation(d));
    } catch (error) {
      throw new Error(`Failed to fetch donations: ${error.message}`);
    }
  }

  // Get donation statistics (for admin dashboard)
  async getDonationStats() {
    try {
      const totalDonations = await MoneyDonation.countDocuments();
      const successfulDonations = await MoneyDonation.countDocuments({ paymentStatus: 'SUCCESS' });
      
      const totalResult = await MoneyDonation.aggregate([
        { $match: { paymentStatus: 'SUCCESS' } },
        { $group: { _id: null, total: { $sum: '$amount' } } }
      ]);

      const avgResult = await MoneyDonation.aggregate([
        { $match: { paymentStatus: 'SUCCESS' } },
        { $group: { _id: null, avg: { $avg: '$amount' } } }
      ]);

      const totalAmount = totalResult.length > 0 ? totalResult[0].total : 0;
      const avgAmount = avgResult.length > 0 ? avgResult[0].avg : 0;

      return {
        totalDonations,
        successfulDonations,
        totalAmount: totalAmount || 0,
        averageDonation: avgAmount || 0,
        successRate: totalDonations > 0 ? ((successfulDonations / totalDonations) * 100).toFixed(2) : 0
      };
    } catch (error) {
      throw new Error(`Failed to fetch statistics: ${error.message}`);
    }
  }

  // Refund a donation
  async refundDonation(donationId, reason) {
    try {
      const donation = await MoneyDonation.findById(donationId);

      if (!donation) {
        throw new Error('Donation not found');
      }

      donation.paymentStatus = 'REFUNDED';
      donation.metadata = {
        ...donation.metadata,
        refundReason: reason,
        refundedAt: new Date().toISOString()
      };

      await donation.save();
      return donation;
    } catch (error) {
      throw new Error(`Failed to refund donation: ${error.message}`);
    }
  }
}

module.exports = new MoneyDonationService();
