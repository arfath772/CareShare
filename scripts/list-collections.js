const mongoose = require('mongoose');
require('dotenv').config();

const uri = process.env.MONGODB_URI;

(async () => {
  try {
    await mongoose.connect(uri);
    const db = mongoose.connection.db;
    const collections = await db.listCollections().toArray();
    
    console.log('\n=== Your MongoDB "Tables" (Collections) ===');
    collections.forEach(col => {
      console.log(`- ${col.name}`);
    });
    console.log('===========================================\n');
    
    await mongoose.connection.close();
    process.exit(0);
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
})();
