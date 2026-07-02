require('dotenv').config();
const {
  connectMongoDB,
  mongoose,
  User,
  Product,
  DonateItem,
  DonateRequest,
  ExchangeRequest,
  PurchaseRequest,
  MoneyDonation
} = require('../models');

/**
 * MongoDB migration helper
 * In MongoDB we do not migrate columns; this script synchronizes indexes.
 */

async function syncModelIndexes(model, name) {
  await model.createCollection();
  const result = await model.syncIndexes();
  const dropped = Array.isArray(result) ? result.length : 0;
  console.log(`  ✅ ${name}: indexes synced${dropped > 0 ? ` (dropped ${dropped} stale index(es))` : ''}`);
}

async function migrateDatabase() {
  try {
    console.log('🔄 Starting MongoDB index migration...\n');
    await connectMongoDB();

    await syncModelIndexes(User, 'users');
    await syncModelIndexes(Product, 'products');
    await syncModelIndexes(DonateItem, 'donateitems');
    await syncModelIndexes(DonateRequest, 'donaterequests');
    await syncModelIndexes(ExchangeRequest, 'exchangerequests');
    await syncModelIndexes(PurchaseRequest, 'purchaserequests');
    await syncModelIndexes(MoneyDonation, 'moneydonations');

    console.log('\n============================================================');
    console.log('✅ MongoDB migration complete (indexes synchronized)');
    console.log('============================================================\n');

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('\n❌ Migration error:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
}

migrateDatabase();
