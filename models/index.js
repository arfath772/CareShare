const sequelize = require('../config/db.config');

// Import all models
const User = require('./user.model');
const Product = require('./product.model');
const DonateItem = require('./donateItem.model');
const DonateRequest = require('./donateRequest.model');
const ExchangeRequest = require('./exchangeRequest.model');
const PurchaseRequest = require('./purchaseRequest.model');

// Define relationships

// User - Product relationship
User.hasMany(Product, { foreignKey: 'userId', as: 'products' });
Product.belongsTo(User, { foreignKey: 'userId', as: 'user' });

// User - DonateItem relationship
User.hasMany(DonateItem, { foreignKey: 'userId', as: 'donatedItems' });
DonateItem.belongsTo(User, { foreignKey: 'userId', as: 'user' });

// DonateItem - DonateRequest relationship
DonateItem.hasMany(DonateRequest, { foreignKey: 'donationId', as: 'requests' });
DonateRequest.belongsTo(DonateItem, { foreignKey: 'donationId', as: 'donateItem' });

// User - DonateRequest relationship (receiver)
User.hasMany(DonateRequest, { foreignKey: 'receiverUserId', as: 'receivedRequests' });
DonateRequest.belongsTo(User, { foreignKey: 'receiverUserId', as: 'receiver' });

// Product - ExchangeRequest relationship
Product.hasMany(ExchangeRequest, { foreignKey: 'targetProductId', as: 'exchangeRequests' });
ExchangeRequest.belongsTo(Product, { foreignKey: 'targetProductId', as: 'targetProduct' });

// User - ExchangeRequest relationship (requester)
User.hasMany(ExchangeRequest, { foreignKey: 'requesterId', as: 'exchangeRequests' });
ExchangeRequest.belongsTo(User, { foreignKey: 'requesterId', as: 'requester' });

// Product - PurchaseRequest relationship
Product.hasMany(PurchaseRequest, { foreignKey: 'productId', as: 'purchases' });
PurchaseRequest.belongsTo(Product, { foreignKey: 'productId', as: 'product' });

// User - PurchaseRequest relationship (buyer)
User.hasMany(PurchaseRequest, { foreignKey: 'buyerId', as: 'purchases' });
PurchaseRequest.belongsTo(User, { foreignKey: 'buyerId', as: 'buyer' });

// Sync database
const syncDatabase = async () => {
  try {
    // Use alter: false to avoid constraint conflicts
    // Tables should already be migrated via migrate-database.js
    await sequelize.sync({ alter: false });
    console.log('✅ Database synced successfully!');
  } catch (error) {
    console.error('❌ Error syncing database:', error.message);
  }
};

module.exports = {
  sequelize,
  User,
  Product,
  DonateItem,
  DonateRequest,
  ExchangeRequest,
  PurchaseRequest,
  syncDatabase
};
