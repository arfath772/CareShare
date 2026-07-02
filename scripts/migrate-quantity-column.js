require('dotenv').config();
const { connectMongoDB, mongoose, DonateRequest } = require('../models');

/**
 * MongoDB data fix for donate_requests quantity.
 * Ensures existing documents have a valid quantity >= 1.
 */

(async () => {
  try {
    console.log('\n🔧 Running MongoDB quantity migration\n');

    await connectMongoDB();

    const missingOrNull = await DonateRequest.updateMany(
      { $or: [{ quantity: { $exists: false } }, { quantity: null }] },
      { $set: { quantity: 1 } }
    );

    const invalidRange = await DonateRequest.updateMany(
      { quantity: { $lt: 1 } },
      { $set: { quantity: 1 } }
    );

    console.log(`✅ Updated missing/null quantity documents: ${missingOrNull.modifiedCount}`);
    console.log(`✅ Updated invalid quantity (<1) documents: ${invalidRange.modifiedCount}`);
    console.log('\n🎉 Quantity migration completed successfully!\n');

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Migration error:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
})();
