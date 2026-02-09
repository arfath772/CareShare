const sequelize = require('../config/db.config');
require('dotenv').config();

/**
 * Comprehensive database migration script
 * Adds all missing columns to match Sequelize models
 */

// Helper function to check if column exists
async function columnExists(tableName, columnName) {
  const [results] = await sequelize.query(
    `SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = '${tableName}' 
     AND COLUMN_NAME = '${columnName}'`
  );
  return results.length > 0;
}

// Helper function to add column safely
async function addColumnIfNotExists(tableName, columnName, columnDefinition) {
  const exists = await columnExists(tableName, columnName);
  if (!exists) {
    await sequelize.query(`ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${columnDefinition}`);
    console.log(`  ✅ Added ${columnName}`);
  } else {
    console.log(`  ⏭️  ${columnName} already exists`);
  }
}

async function migrateDatabase() {
  try {
    console.log('🔄 Starting comprehensive database migration...\n');

    // ========== USERS TABLE ==========
    console.log('📋 Migrating users table...');
    await addColumnIfNotExists('users', 'firstName', "VARCHAR(255) DEFAULT 'User'");
    await addColumnIfNotExists('users', 'lastName', "VARCHAR(255) DEFAULT ''");
    await addColumnIfNotExists('users', 'roles', "JSON DEFAULT ('[\"ROLE_USER\"]')");
    await addColumnIfNotExists('users', 'resetToken', "VARCHAR(255)");
    await addColumnIfNotExists('users', 'resetTokenExpiry', "DATETIME");
    await addColumnIfNotExists('users', 'isAdmin', "TINYINT(1) DEFAULT 0 NOT NULL");
    console.log('');

    // ========== PRODUCTS TABLE ==========
    console.log('📋 Migrating products table...');
    await addColumnIfNotExists('products', 'userId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('products', 'createdAt', "DATETIME DEFAULT CURRENT_TIMESTAMP");
    await addColumnIfNotExists('products', 'approvedAt', "DATETIME");
    await addColumnIfNotExists('products', 'rejectedAt', "DATETIME");
    await addColumnIfNotExists('products', 'rejectionReason', "TEXT");
    console.log('');

    // ========== DONATE_ITEMS TABLE ==========
    console.log('📋 Migrating donate_items table...');
    await addColumnIfNotExists('donate_items', 'userId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('donate_items', 'createdAt', "DATETIME DEFAULT CURRENT_TIMESTAMP");
    await addColumnIfNotExists('donate_items', 'updatedAt', "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
    console.log('');

    // ========== DONATE_REQUESTS TABLE ==========
    console.log('📋 Migrating donate_requests table...');
    await addColumnIfNotExists('donate_requests', 'donationId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('donate_requests', 'receiverUserId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('donate_requests', 'requestedDate', "DATETIME DEFAULT CURRENT_TIMESTAMP");
    await addColumnIfNotExists('donate_requests', 'description', "TEXT");
    await addColumnIfNotExists('donate_requests', 'rejectionReason', "TEXT");
    console.log('');

    // ========== EXCHANGE_REQUESTS TABLE ==========
    console.log('📋 Migrating exchange_requests table...');
    await addColumnIfNotExists('exchange_requests', 'targetProductId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('exchange_requests', 'requesterId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('exchange_requests', 'createdAt', "DATETIME DEFAULT CURRENT_TIMESTAMP");
    await addColumnIfNotExists('exchange_requests', 'exchangeItemName', "VARCHAR(255)");
    await addColumnIfNotExists('exchange_requests', 'exchangeItemCategory', "VARCHAR(255)");
    await addColumnIfNotExists('exchange_requests', 'exchangeItemDescription', "TEXT");
    await addColumnIfNotExists('exchange_requests', 'exchangeItemImages', "JSON");
    await addColumnIfNotExists('exchange_requests', 'additionalMessage', "TEXT");
    await addColumnIfNotExists('exchange_requests', 'status', "VARCHAR(50) DEFAULT 'PENDING'");
    await addColumnIfNotExists('exchange_requests', 'rejectionReason', "TEXT");
    console.log('');

    // ========== PURCHASES TABLE ==========
    console.log('📋 Migrating purchases table...');
    await addColumnIfNotExists('purchases', 'productId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('purchases', 'buyerId', "BIGINT NOT NULL DEFAULT 1");
    await addColumnIfNotExists('purchases', 'createdAt', "DATETIME DEFAULT CURRENT_TIMESTAMP");
    await addColumnIfNotExists('purchases', 'updatedAt', "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
    console.log('');

    console.log('=' .repeat(60));
    console.log('✅ All database migrations completed successfully!');
    console.log('=' .repeat(60));
    
    await sequelize.close();
    process.exit(0);
  } catch (error) {
    console.error('\n❌ Migration error:', error.message);
    if (error.original) {
      console.error('SQL Error:', error.original.sqlMessage);
    }
    console.log('\n💡 Tip: Make sure your database is running and you have proper permissions.\n');
    await sequelize.close();
    process.exit(1);
  }
}

// Run migration
migrateDatabase();
