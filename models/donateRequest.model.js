const { DataTypes } = require('sequelize');
const sequelize = require('../config/db.config');

const DonateRequest = sequelize.define('DonateRequest', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  donationId: {
    type: DataTypes.BIGINT,
    allowNull: false,
    references: {
      model: 'donate_items',
      key: 'id'
    }
  },
  receiverUserId: {
    type: DataTypes.BIGINT,
    allowNull: false,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  status: {
    type: DataTypes.ENUM('PENDING', 'APPROVED', 'REJECTED'),
    defaultValue: 'PENDING',
    allowNull: false
  },
  requestedDate: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    allowNull: false
  },
  description: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  rejectionReason: {
    type: DataTypes.TEXT,
    allowNull: true
  }
}, {
  tableName: 'donate_requests',
  timestamps: false
});

module.exports = DonateRequest;
