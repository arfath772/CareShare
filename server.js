const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');
const cors = require('cors');
require('dotenv').config();

const { syncDatabase } = require('./models');

// Import routes
const authRoutes = require('./routes/auth.routes');
const userRoutes = require('./routes/user.routes');
const productRoutes = require('./routes/product.routes');
const donateRoutes = require('./routes/donate.routes');
const exchangeRequestRoutes = require('./routes/exchangeRequest.routes');
const purchaseRoutes = require('./routes/purchase.routes');
const adminRoutes = require('./routes/admin.routes');

// Import middleware
const { optionalAuth } = require('./middleware/auth.middleware');

const app = express();
const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

// Static files
app.use(express.static(path.join(__dirname, 'src', 'main', 'resources', 'static')));
app.use(express.static(path.join(__dirname, 'src', 'main', 'resources', 'templates')));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/user', userRoutes);
app.use('/api/products', productRoutes);
app.use('/api/donate', donateRoutes);
app.use('/api/exchange-requests', exchangeRequestRoutes);
app.use('/api/purchases', purchaseRoutes);
app.use('/api/admin', adminRoutes);

// Page routes (serve HTML files)
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'index.html'));
});

app.get('/login', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'login.html'));
});

app.get('/register', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'register.html'));
});

app.get('/dashboard', optionalAuth, (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'dashboard.html'));
});

app.get('/forgot-password', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'forgot-password.html'));
});

app.get('/reset-password', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'reset-password.html'));
});

app.get('/admin', optionalAuth, (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'admin.html'));
});

app.get('/donate', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'donate.html'));
});

app.get('/donate-now', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'donate-now.html'));
});

app.get('/request-now', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'request-now.html'));
});

app.get('/browse-donations', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'browse-donations.html'));
});

app.get('/donate-items', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'donate-items.html'));
});

app.get('/why-donation', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'main', 'resources', 'templates', 'why-donation.html'));
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err.stack);
  res.status(err.status || 500).json({
    message: err.message || 'Internal Server Error',
    error: process.env.NODE_ENV === 'development' ? err : {}
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ message: 'Route not found' });
});

// Start server and sync database
const startServer = async () => {
  try {
    // Sync database
    await syncDatabase();

    // Start listening
    app.listen(PORT, () => {
      console.log(`\n🚀 Server is running on http://localhost:${PORT}`);
      console.log(`📂 Environment: ${process.env.NODE_ENV || 'development'}`);
      console.log(`🗄️  Database: ${process.env.DB_NAME} on ${process.env.DB_HOST}:${process.env.DB_PORT}`);
      console.log(`\n✅ CareNShare API is ready to use!\n`);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
};

startServer();

module.exports = app;
