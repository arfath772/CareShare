const { mongoose, connectMongoDB } = require('../config/mongo.config');

// Import all models
const User = require('./user.model');
const Product = require('./product.model');
const DonateItem = require('./donateItem.model');
const DonateRequest = require('./donateRequest.model');
const ExchangeRequest = require('./exchangeRequest.model');
const PurchaseRequest = require('./purchaseRequest.model');
const MoneyDonation = require('./moneyDonation.model');

// Mongoose handles relationships via refs automatically
// No need for explicit relationship definitions like Sequelize

// Export models and connection
module.exports = {
  mongoose,
  User,
  Product,
  DonateItem,
  DonateRequest,
  ExchangeRequest,
  PurchaseRequest,
  MoneyDonation,
  connectMongoDB
};
