const { DonateItem, DonateRequest, User } = require('../models');

class DonateItemService {
  // Add donation
  async addDonation(donationData, files, userId) {
    try {
      const donationImages = Array.isArray(files)
        ? files
          .filter((file) => file && file.buffer)
          .map((file) => ({
            filename: file.originalname,
            contentType: file.mimetype,
            data: file.buffer
          }))
        : [];

      const donation = await DonateItem.create({
        itemType: donationData.itemType,
        category: donationData.itemType,
        itemName: donationData.itemName,
        quantity: donationData.quantity,
        itemCondition: donationData.itemCondition,
        pickupAddress: donationData.pickupAddress,
        donationImages,
        imageUrls: JSON.stringify([]),
        mainImageUrl: null,
        status: 'PENDING',
        userId: userId
      });

      if (donationImages.length > 0) {
        donation.mainImageUrl = `/api/donate/${donation._id.toString()}/image/0`;
        await donation.save();
      }

      return donation;
    } catch (error) {
      console.error('Error adding donation:', error);
      throw new Error('Failed to add donation: ' + error.message);
    }
  }

  // Get available donations (approved - not pending/rejected/claimed)
  async getAvailableDonations(type = null) {
    const filter = {
      status: 'APPROVED'
    };

    if (type && type !== 'all') {
      filter.itemType = type;
    }

    return await DonateItem.find(filter)
      .populate('userId', 'id firstName lastName email phone phoneNumber')
      .sort({ createdAt: -1 });
  }

  // Get user's donations
  async getMyDonations(userId) {
    return await DonateItem.find({ userId })
      .populate('userId', 'id firstName lastName email phone phoneNumber')
      .sort({ createdAt: -1 });
  }

  // Get donation by ID
  async getDonationById(id) {
    const donation = await DonateItem.findById(id)
      .populate('userId', 'id firstName lastName email phone phoneNumber');

    if (donation && donation.imageUrls) {
      try {
        donation.imageUrls = JSON.parse(donation.imageUrls);
      } catch (e) {
        donation.imageUrls = [];
      }
    }

    return donation;
  }

  // Get donation image by index
  async getDonationImage(donationId, imageIndex) {
    const donation = await DonateItem.findById(donationId).select('donationImages imageUrls');
    if (!donation) {
      throw new Error('Donation not found');
    }

    if (Array.isArray(donation.donationImages) && donation.donationImages.length > 0) {
      const index = Number.parseInt(imageIndex, 10);
      if (Number.isNaN(index) || index < 0 || index >= donation.donationImages.length) {
        throw new Error('Image not found');
      }

      const img = donation.donationImages[index];
      return {
        contentType: img.contentType || 'application/octet-stream',
        data: img.data
      };
    }

    let fallbackUrls = [];
    if (donation.imageUrls && typeof donation.imageUrls === 'string') {
      try {
        const parsed = JSON.parse(donation.imageUrls);
        if (Array.isArray(parsed)) fallbackUrls = parsed;
      } catch (error) {
        fallbackUrls = [];
      }
    }

    const fallbackIndex = Number.parseInt(imageIndex, 10);
    if (!Number.isNaN(fallbackIndex) && fallbackIndex >= 0 && fallbackIndex < fallbackUrls.length) {
      return { redirectUrl: fallbackUrls[fallbackIndex] };
    }

    throw new Error('Image not found');
  }

  // Delete donation
  async deleteDonation(id, userId) {
    const donation = await DonateItem.findById(id);
    if (!donation) {
      throw new Error('Donation not found');
    }

    if (donation.userId.toString() !== userId.toString()) {
      throw new Error('You are not authorized to delete this donation');
    }

    await DonateItem.findByIdAndDelete(id);
  }

  // Get pending donations
  async getPendingDonations() {
    return await DonateItem.find({ status: 'PENDING' })
      .populate('userId', 'id firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Get all donations
  async getAllDonations() {
    return await DonateItem.find()
      .populate('userId', 'id firstName lastName email')
      .sort({ createdAt: -1 });
  }

  // Approve donation
  async approveDonation(id) {
    const donation = await DonateItem.findByIdAndUpdate(
      id,
      { status: 'APPROVED' },
      { new: true }
    );

    if (!donation) {
      throw new Error('Donation not found');
    }

    return donation;
  }

  // Reject donation
  async rejectDonation(id, reason) {
    const donation = await DonateItem.findByIdAndUpdate(
      id,
      { status: 'REJECTED', rejectionReason: reason },
      { new: true }
    );

    if (!donation) {
      throw new Error('Donation not found');
    }

    return donation;
  }

  // Adjust donation quantity
  async adjustDonationQuantity(id, delta) {
    const donation = await DonateItem.findById(id);
    if (!donation) {
      throw new Error('Donation not found');
    }

    const currentQuantity = Number(donation.quantity || 0);
    const adjustment = Number(delta || 0);

    if (!Number.isInteger(adjustment) || adjustment === 0) {
      throw new Error('Quantity adjustment must be a non-zero integer');
    }

    const nextQuantity = currentQuantity + adjustment;
    if (nextQuantity < 1) {
      throw new Error('Donation quantity cannot be less than 1');
    }

    donation.quantity = nextQuantity;
    await donation.save();

    return donation;
  }

  // Get donations count by status
  async getCountByStatus(status) {
    return await DonateItem.countDocuments({ status });
  }
}

module.exports = new DonateItemService();
