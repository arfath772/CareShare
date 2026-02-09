require('dotenv').config();
const sequelize = require('../config/db.config');

/**
 * Quick environment and database check script
 * Run this before starting your server to ensure everything is configured
 */

console.log('🔍 Environment Configuration Check\n');

// Check required environment variables
const requiredEnvVars = [
  'DB_NAME',
  'DB_USER',
  'DB_PASSWORD',
  'DB_HOST',
  'JWT_SECRET',
  'MAIL_HOST',
  'MAIL_PORT',
  'MAIL_USER',
  'MAIL_PASSWORD'
];

let missingVars = [];
let configuredVars = [];

console.log('📋 Environment Variables:');
requiredEnvVars.forEach(varName => {
  if (!process.env[varName] || process.env[varName].includes('your_')) {
    missingVars.push(varName);
    console.log(`  ❌ ${varName}: NOT SET or using placeholder`);
  } else {
    configuredVars.push(varName);
    // Mask sensitive values
    const value = ['PASSWORD', 'SECRET'].some(s => varName.includes(s)) 
      ? '***' 
      : process.env[varName];
    console.log(`  ✅ ${varName}: ${value}`);
  }
});

console.log('\n');

// Test database connection
console.log('🗄️  Database Connection Test:');
(async () => {
  try {
    await sequelize.authenticate();
    console.log('  ✅ Database connection successful!\n');

    // Check if users table exists and its structure
    const [results] = await sequelize.query("SHOW TABLES LIKE 'users'");
    
    if (results.length === 0) {
      console.log('⚠️  Users table does not exist yet. Server will create it on first run.\n');
    } else {
      console.log('  ✅ Users table exists');
      
      // Check table structure
      const [columns] = await sequelize.query("DESCRIBE users");
      const columnNames = columns.map(col => col.Field);
      
      console.log('\n📊 Users Table Structure:');
      const requiredColumns = ['id', 'email', 'password', 'firstName', 'lastName', 'roles', 'resetToken', 'resetTokenExpiry', 'isAdmin'];
      
      const missingColumns = requiredColumns.filter(col => !columnNames.includes(col));
      const existingColumns = requiredColumns.filter(col => columnNames.includes(col));
      
      existingColumns.forEach(col => {
        console.log(`  ✅ ${col}`);
      });
      
      if (missingColumns.length > 0) {
        console.log('\n⚠️  Missing columns (need migration):');
        missingColumns.forEach(col => {
          console.log(`  ❌ ${col}`);
        });
        console.log('\n💡 Run: npm run migrate');
      }
    }

    console.log('\n' + '='.repeat(60));
    
    // Final summary
    if (missingVars.length > 0) {
      console.log('\n❌ Configuration Issues Found!\n');
      console.log('Missing/Incomplete Environment Variables:');
      missingVars.forEach(v => console.log(`  - ${v}`));
      console.log('\n📝 Please update your .env file with actual values.');
      console.log('   See .env.example and TROUBLESHOOTING.md for guidance.\n');
      process.exit(1);
    } else if (results.length > 0 && missingColumns && missingColumns.length > 0) {
      console.log('\n⚠️  Database schema needs updating!\n');
      console.log('Run: npm run migrate\n');
      process.exit(1);
    } else {
      console.log('\n✅ All checks passed! Your environment is ready.\n');
      console.log('Start the server with: npm start\n');
      process.exit(0);
    }

  } catch (error) {
    console.log('  ❌ Database connection failed!');
    console.log(`  Error: ${error.message}\n`);
    
    if (error.code === 'ER_ACCESS_DENIED_ERROR') {
      console.log('💡 Fix: Check your DB_USER and DB_PASSWORD in .env file');
    } else if (error.code === 'ER_BAD_DB_ERROR') {
      console.log('💡 Fix: Create the database first:');
      console.log(`   mysql -u root -p -e "CREATE DATABASE ${process.env.DB_NAME}"`);
    } else if (error.code === 'ECONNREFUSED') {
      console.log('💡 Fix: Make sure MySQL server is running');
    }
    
    console.log('\nSee TROUBLESHOOTING.md for detailed help.\n');
    process.exit(1);
  }
})();
