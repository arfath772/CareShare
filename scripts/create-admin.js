const { connectMongoDB, mongoose, User } = require('../models');
require('dotenv').config();

async function createAdmin() {
  try {
    console.log('🔐 Creating admin account...');

    const email = 'mohammedarfath46982@gmail.com';
    const password = '9113611658';
    await connectMongoDB();

    // Check if user already exists
    const existing = await User.findOne({ email: email.toLowerCase() });

    if (existing) {
      console.log('⚠️  User already exists. Promoting to admin...');
      const roles = Array.isArray(existing.roles) ? existing.roles : ['ROLE_USER'];
      existing.isAdmin = true;
      existing.roles = [...new Set([...roles, 'ROLE_ADMIN', 'ROLE_USER'])];
      await existing.save();
      console.log('✅ User promoted to admin successfully!');
    } else {
      console.log('➕ Creating new admin user...');
      await User.create({
        email: email.toLowerCase(),
        password,
        firstName: 'Mohammed',
        lastName: 'Arfath',
        roles: ['ROLE_ADMIN', 'ROLE_USER'],
        isAdmin: true,
        accountType: 'USER',
        ngoStatus: 'NOT_REQUIRED'
      });
      console.log('✅ Admin account created successfully!');
    }

    console.log('\n📧 Email: ' + email);
    console.log('🔑 Password: ' + password);
    console.log('👑 Admin: Yes');
    console.log('\n✅ You can now login at: http://localhost:8080/login\n');

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error creating admin:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
}

createAdmin();
