require('dotenv').config();
const { connectMongoDB, mongoose } = require('../models');

/**
 * Quick environment and database check script
 * Run this before starting your server to ensure everything is configured
 */

console.log('🔍 Environment Configuration Check\n');

// Check required environment variables
const requiredEnvVars = [
  'MONGODB_URI',
  'JWT_SECRET',
  'APP_BASE_URL'
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
    await connectMongoDB();
    console.log('  ✅ Database connection successful!\n');

    const usersExists = (await mongoose.connection.db.listCollections({ name: 'users' }).toArray()).length > 0;
    const productsExists = (await mongoose.connection.db.listCollections({ name: 'products' }).toArray()).length > 0;

    console.log(`  ${usersExists ? '✅' : '⚠️ '} users collection ${usersExists ? 'exists' : 'not found yet (created on first write)'}`);
    console.log(`  ${productsExists ? '✅' : '⚠️ '} products collection ${productsExists ? 'exists' : 'not found yet (created on first write)'}`);

    await mongoose.connection.close();

    console.log('\n' + '='.repeat(60));
    
    // Final summary
    if (missingVars.length > 0) {
      console.log('\n❌ Configuration Issues Found!\n');
      console.log('Missing/Incomplete Environment Variables:');
      missingVars.forEach(v => console.log(`  - ${v}`));
      console.log('\n📝 Please update your .env file with actual values.');
      console.log('   See .env.example and TROUBLESHOOTING.md for guidance.\n');
      process.exit(1);
    } else {
      console.log('\n✅ All checks passed! Your environment is ready.\n');
      console.log('Start the server with: npm start\n');
      process.exit(0);
    }

  } catch (error) {
    console.log('  ❌ Database connection failed!');
    console.log(`  Error: ${error.message}\n`);

    if (error.message.includes('not allowed to do action')) {
      console.log('💡 Fix: Grant readWrite role to your Atlas DB user on database careshare');
    } else if (error.message.includes('bad auth')) {
      console.log('💡 Fix: Check username/password in MONGODB_URI');
    } else if (error.message.includes('ECONNREFUSED') || error.message.includes('Server selection timed out')) {
      console.log('💡 Fix: Check Atlas network access and cluster availability');
    }

    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    console.log('\nSee TROUBLESHOOTING.md for detailed help.\n');
    process.exit(1);
  }
})();
