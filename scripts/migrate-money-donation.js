require('dotenv').config();
const { connectMongoDB, mongoose, MoneyDonation } = require('../models');

async function runMigration() {
  try {
    console.log('🔄 Starting money donation MongoDB migration...');

    await connectMongoDB();
    await MoneyDonation.createCollection();
    const result = await MoneyDonation.syncIndexes();

    console.log(`✅ moneydonations collection ready${Array.isArray(result) && result.length > 0 ? ` (dropped ${result.length} stale index(es))` : ''}`);
    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Migration error:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
}

runMigration();
