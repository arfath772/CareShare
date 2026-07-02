const mongoose = require('mongoose');
const dns = require('dns');
dns.setServers(['8.8.8.8', '8.8.4.4']);
require('dotenv').config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/careshare';
const MAX_RETRIES = parseInt(process.env.MONGO_MAX_RETRIES || '5', 10);
let retryCount = 0;

// Set mongoose options and event handlers for better stability
mongoose.set('strictQuery', false);

mongoose.connection.on('connected', () => {
  console.log('🟢 Mongoose default connection is open');
});

mongoose.connection.on('error', (err) => {
  console.error('⛔ Mongoose default connection error:', err && err.message ? err.message : err);
});

mongoose.connection.on('disconnected', () => {
  console.warn('⚠️ Mongoose default connection is disconnected');
});

mongoose.connection.on('reconnected', () => {
  console.log('🔁 Mongoose reconnected to MongoDB');
});

let replSet = null;

const startInMemoryReplSet = async () => {
  try {
    console.log('🔄 Starting local in-memory MongoDB Replica Set...');
    const { MongoMemoryReplSet } = require('mongodb-memory-server');
    replSet = await MongoMemoryReplSet.create({
      replSet: { storageEngine: 'wiredTiger' } // wiredTiger is required for transactions/sessions
    });
    const uri = replSet.getUri();
    console.log('✅ Local in-memory MongoDB Replica Set started!');
    return uri;
  } catch (error) {
    console.error('❌ Failed to start in-memory MongoDB replica set:', error);
    throw error;
  }
};

const connectMongoDB = async () => {
  const useInMemory = process.env.USE_IN_MEMORY_DB === 'true';
  let connectionUri = MONGODB_URI;

  if (useInMemory && !replSet) {
    try {
      connectionUri = await startInMemoryReplSet();
    } catch (err) {
      console.warn('⚠️  Could not start in-memory DB, falling back to default URI');
    }
  } else if (replSet) {
    connectionUri = replSet.getUri();
  }

  try {
    console.log('🔄 Connecting to MongoDB...');
    console.log('📍 Connection String:', connectionUri.includes('@') ? connectionUri.split('@')[0] + '@***' : connectionUri);

    await mongoose.connect(connectionUri, {
      serverSelectionTimeoutMS: parseInt(process.env.MONGO_SERVER_SELECTION_TIMEOUT_MS || '30000', 10),
      socketTimeoutMS: parseInt(process.env.MONGO_SOCKET_TIMEOUT_MS || '45000', 10),
      connectTimeoutMS: parseInt(process.env.MONGO_CONNECT_TIMEOUT_MS || '15000', 10),
      family: parseInt(process.env.MONGO_FAMILY || '4', 10),
      tlsAllowInvalidCertificates: true,
      retryWrites: true,
      w: 'majority',
    });

    console.log('✅ MongoDB connected successfully!');
    return true;
  } catch (error) {
    retryCount++;
    console.error(`❌ MongoDB connection failed (Attempt ${retryCount}/${MAX_RETRIES}):`, error && error.message ? error.message : error);

    if (retryCount < MAX_RETRIES) {
      const delay = Math.min(3000 * retryCount, 30000);
      console.log(`⏳ Retrying in ${delay / 1000} seconds...`);
      await new Promise(resolve => setTimeout(resolve, delay));
      return connectMongoDB();
    }

    // Fallback to in-memory Replica Set if remote connection failed and we haven't already tried to start it
    if (!useInMemory && !replSet) {
      console.warn('⚠️  MongoDB initial connection failed after retries. Attempting to fall back to in-memory Replica Set...');
      try {
        const fallbackUri = await startInMemoryReplSet();
        console.log('🔄 Connecting to fallback in-memory MongoDB...');
        await mongoose.connect(fallbackUri, {
          serverSelectionTimeoutMS: 15000,
          socketTimeoutMS: 45000,
          connectTimeoutMS: 15000,
          family: 4,
          tlsAllowInvalidCertificates: true,
          retryWrites: true,
          w: 'majority',
        });
        console.log('✅ Fallback MongoDB connected successfully!');
        return true;
      } catch (fallbackError) {
        console.error('❌ Fallback MongoDB connection failed:', fallbackError);
      }
    }

    console.warn('⚠️  MongoDB initial connection failed after retries.');
    console.warn('💡 The app may continue in DEMO MODE; mongoose will keep attempting to reconnect in the background.');
    return false;
  }
};

module.exports = { mongoose, connectMongoDB };
