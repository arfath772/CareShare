const mongoose = require('mongoose');
const dns = require('dns');
dns.setServers(['8.8.8.8', '8.8.4.4']);

const uri = 'mongodb+srv://careshare:VNrAcksg5aS7OIpb@cluster0.qh3dmtn.mongodb.net/careshare?retryWrites=true&w=majority&appName=Cluster0';

(async () => {
  try {
    console.log('Connecting to MongoDB...');
    await mongoose.connect(uri, { 
      serverSelectionTimeoutMS: 10000,
      tlsAllowInvalidCertificates: true
    });
    console.log('MongoDB connected (test script).');
    await mongoose.connection.close();
    process.exit(0);
  } catch (err) {
    console.error('Connect error:', err);
    if (err.reason && err.reason.servers) {
      for (const [key, value] of err.reason.servers.entries()) {
        console.error(`Server ${key} error:`, value.error);
      }
    }
    process.exit(1);
  }
})();
