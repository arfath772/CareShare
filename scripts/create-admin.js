const sequelize = require('../config/db.config');
const bcrypt = require('bcryptjs');
require('dotenv').config();

async function createAdmin() {
  try {
    console.log('🔐 Creating admin account...');

    const email = 'mohammedarfath46982@gmail.com';
    const password = '9113611658';
    const hashedPassword = await bcrypt.hash(password, 10);

    // Check if user already exists
    const [existing] = await sequelize.query(
      'SELECT id, isAdmin FROM users WHERE email = ?',
      { replacements: [email] }
    );

    if (existing.length > 0) {
      console.log('⚠️  User already exists. Promoting to admin...');
      await sequelize.query(
        `UPDATE users 
         SET isAdmin = 1, 
             roles = JSON_ARRAY('ROLE_ADMIN', 'ROLE_USER')
         WHERE email = ?`,
        { replacements: [email] }
      );
      console.log('✅ User promoted to admin successfully!');
    } else {
      console.log('➕ Creating new admin user...');
      await sequelize.query(
        `INSERT INTO users (email, password, firstName, lastName, roles, isAdmin)
         VALUES (?, ?, 'Mohammed', 'Arfath', JSON_ARRAY('ROLE_ADMIN', 'ROLE_USER'), 1)`,
        { replacements: [email, hashedPassword] }
      );
      console.log('✅ Admin account created successfully!');
    }

    console.log('\n📧 Email: ' + email);
    console.log('🔑 Password: ' + password);
    console.log('👑 Admin: Yes');
    console.log('\n✅ You can now login at: http://localhost:8080/login\n');
    
    await sequelize.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error creating admin:', error.message);
    if (error.original) {
      console.error('Details:', error.original.message);
    }
    process.exit(1);
  }
}

createAdmin();
