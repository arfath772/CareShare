const { User } = require('../models');

async function checkAdmin() {
  try {
    const admin = await User.findOne({
      where: { email: 'mohammedarfath46982@gmail.com' },
      attributes: ['id', 'email', 'firstName', 'lastName', 'isAdmin', 'roles']
    });

    if (!admin) {
      console.log('❌ Admin user not found!');
      return;
    }

    console.log('\n✅ Admin user found:');
    console.log('   Email:', admin.email);
    console.log('   Name:', admin.firstName, admin.lastName);
    console.log('   Is Admin:', admin.isAdmin);
    console.log('   Roles:', admin.roles);
    
    if (admin.isAdmin) {
      console.log('\n✅ Admin privileges are correctly set!');
      console.log('   Login at: http://localhost:8080/login');
      console.log('   You will be redirected to: http://localhost:8080/admin\n');
    } else {
      console.log('\n❌ Warning: isAdmin is false! Run: npm run create-admin\n');
    }
    
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }
}

checkAdmin();
