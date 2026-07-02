require('dotenv').config();
const { connectMongoDB } = require('../config/mongo.config');
const User = require('../models/user.model');

(async () => {
  try {
    const ok = await connectMongoDB();
    if (!ok) {
      console.error('DB connection failed; aborting smoke test');
      process.exit(1);
    }

    const email = process.env.SMOKE_USER_EMAIL || 'smoke.test@example.com';
    const existing = await User.findOne({ email });
    if (existing) {
      console.log('Smoke user already exists:', existing.email);
      process.exit(0);
    }

    const user = new User({
      email,
      password: process.env.SMOKE_USER_PASSWORD || 'P@ssw0rd!',
      firstName: 'Smoke',
      lastName: 'Tester'
    });

    await user.save();
    console.log('Smoke user created:', user.email);
    process.exit(0);
  } catch (err) {
    console.error('Smoke test error:', err && err.message ? err.message : err);
    process.exit(1);
  }
})();
