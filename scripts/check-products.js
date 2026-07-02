require('dotenv').config();
const { connectMongoDB, mongoose, Product } = require('../models');

(async () => {
  try {
    await connectMongoDB();
    console.log('\n🔍 Product Collection Inspection\n');

    const products = await Product.find()
      .populate('userId', 'firstName lastName email')
      .sort({ createdAt: -1 });

    console.log(`Total Products: ${products.length}\n`);

    products.forEach((p, idx) => {
      console.log(`Product #${idx + 1}:`);
      console.log(`  ID: ${p._id}`);
      console.log(`  Name: ${p.name}`);
      console.log(`  Type: ${p.type}`);
      console.log(`  Price: ${p.price}`);
      console.log(`  Status: ${p.status}`);
      console.log(`  imagePath: ${p.imagePath}`);
      console.log(`  imagePaths: ${JSON.stringify(p.imagePaths)}`);
      console.log(`  Seller: ${p.userId ? p.userId.email : 'Unknown'}`);
      console.log('----------------------------------------------------');
    });

    await mongoose.connection.close();
  } catch (error) {
    console.error('❌ Error:', error);
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
    }
  }
})();
