require('dotenv').config();
const { connectMongoDB, mongoose, DonateItem } = require('../models');

/**
 * Check donations in the database
 * Helps diagnose why NGO users aren't seeing donations
 */

(async () => {
  try {
    await connectMongoDB();
    console.log('\n🔍 Donation Status Check\n');

    // Get counts by status
    const donationCounts = await DonateItem.aggregate([
      { $group: { _id: '$status', count: { $sum: 1 } } },
      { $sort: { _id: 1 } }
    ]);

    console.log('📊 Donations by Status:');
    let totalDonations = 0;
    donationCounts.forEach(row => {
      console.log(`  ${row._id}: ${row.count}`);
      totalDonations += parseInt(row.count);
    });
    console.log(`  Total: ${totalDonations}\n`);

    if (totalDonations === 0) {
      console.log('⚠️  No donations found in database!');
      console.log('\n📝 To add test donations:');
      console.log('   1. Go to http://localhost:8080/donate');
      console.log('   2. Login as a regular user (not NGO)');
      console.log('   3. Fill in donation details and submit');
      console.log('   4. Go to http://localhost:8080/admin');
      console.log('   5. Approve the pending donations');
      console.log('   6. Login as NGO user to see approved donations\n');
      await mongoose.connection.close();
      process.exit(0);
    }

    // Get approved donations (what NGOs should see)
    const approvedDonations = await DonateItem.find({ status: 'APPROVED' })
      .populate('userId', 'firstName lastName email')
      .select('itemName itemType quantity status');

    console.log('✅ Approved Donations (Visible to NGOs):');
    if (approvedDonations.length === 0) {
      console.log('   None found');
      console.log('\n💡 Solution: Admin needs to approve pending donations');
      console.log('   Go to http://localhost:8080/admin and approve donations\n');
    } else {
      approvedDonations.forEach(d => {
        console.log(`   - ${d.itemName} (${d.itemType}) - Qty: ${d.quantity}`);
      });
      console.log();
    }

    // Get pending donations
    const pendingDonations = await DonateItem.find({ status: 'PENDING' })
      .populate('userId', 'firstName lastName');

    if (pendingDonations.length > 0) {
      console.log(`⏳ Pending Donations (Awaiting Approval): ${pendingDonations.length}`);
      pendingDonations.slice(0, 5).forEach(d => {
        const donor = d.userId ? `${d.userId.firstName} ${d.userId.lastName}` : 'Unknown Donor';
        console.log(`   - ${d.itemName} by ${donor}`);
      });
      if (pendingDonations.length > 5) {
        console.log(`   ... and ${pendingDonations.length - 5} more`);
      }
      console.log();
    }

    console.log('✨ All donations (including pending) should now show in NGO dashboard!\n');
    await mongoose.connection.close();

  } catch (error) {
    console.error('❌ Error:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
})();
