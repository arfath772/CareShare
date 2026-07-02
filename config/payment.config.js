const Razorpay = require('razorpay');
require('dotenv').config();

let razorpayInstance = null;

// Initialize Razorpay only if credentials are available
if (process.env.RAZORPAY_KEY_ID && process.env.RAZORPAY_KEY_SECRET) {
  razorpayInstance = new Razorpay({
    key_id: process.env.RAZORPAY_KEY_ID,
    key_secret: process.env.RAZORPAY_KEY_SECRET
  });
} else {
  razorpayInstance = {
    orders: {
      create: async (options) => ({
        id: 'order_mock_' + Date.now(),
        amount: options.amount,
        currency: options.currency,
        status: 'created'
      })
    },
    payments: {
      fetch: async (paymentId) => ({
        id: paymentId,
        status: 'captured',
        amount: 10000,
        currency: 'INR'
      }),
      refund: async (paymentId, options) => ({
        id: 'refund_mock_' + Date.now(),
        status: 'processed',
        amount: options.amount || 10000
      })
    }
  };
}

module.exports = {
  razorpay: razorpayInstance,
  razorpayKeyId: process.env.RAZORPAY_KEY_ID || 'mock_key',
  razorpayKeySecret: process.env.RAZORPAY_KEY_SECRET || 'mock_secret',
  razorpayWebhookSecret: process.env.RAZORPAY_WEBHOOK_SECRET || 'mock_secret',
  minAmount: 1,
  maxAmount: 100000,
  currency: 'INR'
};
