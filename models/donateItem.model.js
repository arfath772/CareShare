const { DataTypes } = require('sequelize');
const sequelize = require('../config/db.config');

const DonateItem = sequelize.define('DonateItem', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  itemType: {
    type: DataTypes.STRING,
    allowNull: false
  },
  itemName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  quantity: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  itemCondition: {
    type: DataTypes.STRING,
    allowNull: false
  },
  pickupAddress: {
    type: DataTypes.TEXT,
    allowNull: false
  },
  imageUrls: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  mainImageUrl: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  status: {
    type: DataTypes.ENUM('PENDING', 'APPROVED', 'REJECTED', 'CLAIMED'),
    defaultValue: 'PENDING',
    allowNull: false
  },
  rejectionReason: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  userId: {
    type: DataTypes.BIGINT,
    allowNull: false,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    allowNull: false
  },
  updatedAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    allowNull: false
  }
}, {
  tableName: 'donate_items',
  timestamps: false
});

module.exports = DonateItem;
