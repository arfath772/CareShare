const { DataTypes } = require('sequelize');
const sequelize = require('../config/db.config');

const ExchangeRequest = sequelize.define('ExchangeRequest', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  targetProductId: {
    type: DataTypes.BIGINT,
    allowNull: false,
    references: {
      model: 'products',
      key: 'id'
    }
  },
  exchangeItemName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  exchangeItemCategory: {
    type: DataTypes.STRING,
    allowNull: false
  },
  exchangeItemDescription: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  exchangeItemImages: {
    type: DataTypes.JSON,
    defaultValue: []
  },
  additionalMessage: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  requesterId: {
    type: DataTypes.BIGINT,
    allowNull: false,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  status: {
    type: DataTypes.STRING,
    defaultValue: 'PENDING',
    allowNull: false
  },
  rejectionReason: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    allowNull: false
  }
}, {
  tableName: 'exchange_requests',
  timestamps: false
});

module.exports = ExchangeRequest;
