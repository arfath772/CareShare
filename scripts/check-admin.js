const { connectMongoDB, mongoose, User } = require('../models');

async function checkAdmin() {
  try {
    await connectMongoDB();
    const admin = await User.findOne({ email: 'mohammedarfath46982@gmail.com' })
      .select('_id email firstName lastName isAdmin roles');

    if (!admin) {
      console.log('❌ Admin user not found!');
      await mongoose.connection.close();
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

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
    process.exit(1);
  }
}

checkAdmin();
