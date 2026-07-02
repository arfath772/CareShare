require('dotenv').config();
const { connectMongoDB, mongoose, DonateItem, User } = require('../models');

/**
 * Create test donations for NGO dashboard testing
 * This creates sample donations in APPROVED status so NGOs can see and request them
 */

(async () => {
  try {
    await connectMongoDB();
    console.log('\n🎯 Creating Test Donations for NGO Dashboard\n');

    // Create or find test donor user
    let testDonor = await User.findOne({ email: 'donor@test.com' });

    if (!testDonor) {
      console.log('📝 Creating test donor user...');
      testDonor = new User({
        email: 'donor@test.com',
        password: 'password123',
        firstName: 'Test',
        lastName: 'Donor',
        phoneNumber: '9999999999',
        accountType: 'USER',
        isAdmin: false,
        roles: ['ROLE_USER']
      });
      await testDonor.save();
      console.log('✅ Test donor created\n');
    } else {
      console.log('✅ Using existing test donor\n');
    }

    // Sample test donations
    const testDonations = [
      {
        itemType: 'Clothes',
        itemName: 'Winter Jackets Bundle',
        quantity: 15,
        itemCondition: 'GOOD',
        pickupAddress: '123 Charity Lane, Community Center',
        mainImageUrl: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?auto=format&fit=crop&w=400&q=60',
        status: 'APPROVED'
      },
      {
        itemType: 'Books',
        itemName: 'Educational Books Collection',
        quantity: 50,
        itemCondition: 'LIKE_NEW',
        pickupAddress: '456 Library Street, Main Library',
        mainImageUrl: 'https://images.unsplash.com/photo-1507842217343-583f20270319?auto=format&fit=crop&w=400&q=60',
        status: 'APPROVED'
      },
      {
        itemType: 'Electronics',
        itemName: 'Used Laptops & Tablets',
        quantity: 8,
        itemCondition: 'GOOD',
        pickupAddress: '789 Tech Tower, Downtown',
        mainImageUrl: 'https://images.unsplash.com/photo-1588872657840-790ff3bde172?auto=format&fit=crop&w=400&q=60',
        status: 'APPROVED'
      },
      {
        itemType: 'Furniture',
        itemName: 'School Desks and Chairs',
        quantity: 20,
        itemCondition: 'FAIR',
        pickupAddress: '321 Community Hall, Central District',
        mainImageUrl: 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=400&q=60',
        status: 'APPROVED'
      },
      {
        itemType: 'Clothes',
        itemName: 'School Uniforms',
        quantity: 100,
        itemCondition: 'NEW',
        pickupAddress: '654 Supply Warehouse, East Side',
        mainImageUrl: 'https://images.unsplash.com/photo-1598301257942-6fdf82f21b3c?auto=format&fit=crop&w=400&q=60',
        status: 'PENDING'
      },
      {
        itemType: 'Food',
        itemName: 'Canned Food Donation',
        quantity: 200,
        itemCondition: 'NEW',
        pickupAddress: '111 Food Bank, North Avenue',
        mainImageUrl: 'https://images.unsplash.com/photo-1599599810694-b5ac4dd64bed?auto=format&fit=crop&w=400&q=60',
        status: 'APPROVED'
      }
    ];

    console.log('📦 Creating test donations...\n');

    const created = [];
    for (const donation of testDonations) {
      // Check if donation already exists
      const exists = await DonateItem.findOne({ itemName: donation.itemName, userId: testDonor._id });

      if (!exists) {
        const newDonation = await DonateItem.create({
          ...donation,
          userId: testDonor._id,
          imageUrls: JSON.stringify([donation.mainImageUrl])
        });
        created.push(newDonation);
        console.log(`  ✅ ${donation.itemName} (${donation.status})`);
      } else {
        console.log(`  ⏭️  ${donation.itemName} (already exists)`);
      }
    }

    console.log(`\n✨ ${created.length} new test donations created!\n`);

    // Get counts
    const counts = await DonateItem.aggregate([
      { $group: { _id: '$status', count: { $sum: 1 } } },
      { $sort: { _id: 1 } }
    ]);

    console.log('📊 Updated Donation Counts:');
    counts.forEach(row => {
      console.log(`  ${row._id}: ${row.count}`);
    });

    console.log('\n🎉 You can now login as an NGO user to see these donations!');
    console.log('🔗 Visit: http://localhost:8080/login\n');

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error creating test donations:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
})();
