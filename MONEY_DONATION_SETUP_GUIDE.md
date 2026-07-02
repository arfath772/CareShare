# Money Donation Feature - Setup & Configuration Guide

## ✅ Implementation Complete!

The money donation feature has been successfully implemented with the following components:

---

## 📋 What Was Implemented

### Backend Components Created:
1. ✅ **Models**: `moneyDonation.model.js` - Database schema for money donations
2. ✅ **Services**: 
   - `moneyDonation.service.js` - Donation business logic
   - `payment.service.js` - Razorpay payment integration
   - `receipt.service.js` - PDF receipt generation
3. ✅ **Controllers**: `moneyDonation.controller.js` - API endpoints
4. ✅ **Routes**: `moneyDonation.routes.js` - Route definitions
5. ✅ **Utilities**: 
   - `payment-utils.js` - Payment helpers
   - `receipt-generator.js` - PDF generation
6. ✅ **Config**: `payment.config.js` - Razorpay configuration
7. ✅ **Admin Features**: Updated admin controller & routes for donation management
8. ✅ **Database**: MoneyDonations table created
9. ✅ **Frontend**: `donate-money.html` - User donation interface

---

## 🔧 Configuration Setup

### Step 1: Update Your `.env` File

Add the following environment variables:

```env
# Razorpay Configuration
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxxxxxxxxxxx

# Company Details
COMPANY_NAME=CareShare
COMPANY_ADDRESS=123 Care Street, Charity City, CC 12345
COMPANY_EMAIL=contact@careshare.com
COMPANY_PHONE=+91-9876543210
COMPANY_REGISTRATION_NUMBER=REG123456
COMPANY_TAX_ID=29ABCDE1234F1Z0

# PDF & Receipt Settings
RECEIPT_LOGO_PATH=./assets/images/logo.png
PDF_GENERATION_TIMEOUT=30000

# Payment Limits (in ₹)
MIN_DONATION_AMOUNT=1
MAX_DONATION_AMOUNT=100000
```

### Step 2: Get Razorpay Credentials

1. Go to [https://razorpay.com](https://razorpay.com)
2. Sign up for a free account
3. Navigate to Settings → API Keys
4. Copy your:
   - Key ID (for RAZORPAY_KEY_ID)
   - Key Secret (for RAZORPAY_KEY_SECRET)
5. Set up webhook secret in webhook settings

### Step 3: Create Receipts Directory

The `/uploads/receipts` directory is automatically created by the receipt service, but ensure `/uploads` folder exists.

---

## 🌐 API Endpoints

### User Endpoints

#### 1. Initiate Donation
```
POST /api/donations/money/initiate
Content-Type: application/json

{
  "amount": 100,
  "donorName": "John Doe",
  "donorEmail": "john@example.com",
  "donorPhone": "+91-9876543210",
  "message": "Keep up the good work!",
  "isAnonymous": false,
  "paymentMethod": "RAZORPAY"
}

Response: {
  "success": true,
  "data": {
    "donationId": 1,
    "receiptNumber": "RCP-20260429-ABC123",
    "transactionId": "TXN-1234567890-XYZ789",
    "order": {
      "orderId": "order_xxxxxx",
      "amount": 10000,
      "currency": "INR"
    },
    "razorpayKeyId": "rzp_test_xxxxx"
  }
}
```

#### 2. Verify Payment
```
POST /api/donations/money/verify-payment
Content-Type: application/json

{
  "donationId": 1,
  "orderId": "order_xxxxxx",
  "paymentId": "pay_xxxxxx",
  "signature": "xxxxx"
}

Response: {
  "success": true,
  "data": {
    "donationId": 1,
    "paymentStatus": "SUCCESS",
    "receiptNumber": "RCP-20260429-ABC123",
    "receiptUrl": "/uploads/receipts/receipt_RCP-20260429-ABC123_1234567890.pdf"
  }
}
```

#### 3. Get Donation Details
```
GET /api/donations/money/details/:donationId
Headers: Authorization: Bearer <token>
```

#### 4. Download Receipt
```
GET /api/donations/money/download/:donationId
Headers: Authorization: Bearer <token>

Response: PDF file download
```

#### 5. Get User's Donations
```
GET /api/donations/money/my-donations
Headers: Authorization: Bearer <token>
```

### Admin Endpoints

#### 1. Get All Money Donations
```
GET /api/admin/money-donations?status=SUCCESS&limit=20&offset=0
Headers: Authorization: Bearer <admin-token>
```

#### 2. Get Money Donation Statistics
```
GET /api/admin/money-donations/stats
Headers: Authorization: Bearer <admin-token>

Response: {
  "success": true,
  "data": {
    "totalDonations": 150,
    "successfulDonations": 145,
    "totalAmount": 50000,
    "averageDonation": 333.33,
    "successRate": "96.67"
  }
}
```

#### 3. Refund a Donation
```
POST /api/admin/money-donations/:donationId/refund
Headers: Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "reason": "Duplicate transaction"
}
```

### Webhook Endpoint

#### Razorpay Webhook
```
POST /api/donations/money/webhook/razorpay
```

---

## 🎨 Frontend Usage

### Money Donation Page

The donation page is available at:
```
http://localhost:8080/donate-money.html
```

**Features:**
- ✅ Amount presets (₹100, ₹500, ₹1000, ₹2000, ₹5000)
- ✅ Custom amount input
- ✅ Donor information form
- ✅ Anonymous donation option
- ✅ Optional message
- ✅ Real-time Razorpay checkout
- ✅ Automatic receipt generation
- ✅ PDF download after payment

### Add Donation Link to Navigation

Add this to your navigation/menu template:

```html
<a href="/donate-money.html" class="nav-link">💝 Donate Money</a>
```

---

## 📊 Admin Dashboard Integration

### Add to Admin Dashboard Template

```html
<div class="admin-money-donations">
  <h2>💰 Money Donations</h2>
  
  <div class="stats-cards">
    <div class="stat-card">
      <h4>Total Donations</h4>
      <p class="stat-value" id="totalDonations">0</p>
    </div>
    <div class="stat-card">
      <h4>Total Amount</h4>
      <p class="stat-value" id="totalAmount">₹0</p>
    </div>
    <div class="stat-card">
      <h4>Successful Donations</h4>
      <p class="stat-value" id="successfulDonations">0</p>
    </div>
    <div class="stat-card">
      <h4>Average Donation</h4>
      <p class="stat-value" id="averageDonation">₹0</p>
    </div>
  </div>
  
  <div class="donations-table">
    <h3>Recent Donations</h3>
    <table>
      <thead>
        <tr>
          <th>Receipt #</th>
          <th>Donor Name</th>
          <th>Amount</th>
          <th>Status</th>
          <th>Date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody id="donationsTableBody">
        <tr><td colspan="6">Loading...</td></tr>
      </tbody>
    </table>
  </div>
</div>
```

### Admin Dashboard JavaScript

```javascript
async function loadDonationStats() {
  const statsResponse = await fetch('/api/admin/money-donations/stats', {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('adminToken')
    }
  });
  
  const statsData = await statsResponse.json();
  
  if (statsData.success) {
    const stats = statsData.data;
    document.getElementById('totalDonations').textContent = stats.totalDonations;
    document.getElementById('totalAmount').textContent = '₹' + parseFloat(stats.totalAmount).toFixed(2);
    document.getElementById('successfulDonations').textContent = stats.successfulDonations;
    document.getElementById('averageDonation').textContent = '₹' + parseFloat(stats.averageDonation).toFixed(2);
  }
}

// Call on page load
loadDonationStats();
```

---

## 🧪 Testing Instructions

### 1. Test with Razorpay Test Credentials

**Test Card Details:**
- Card Number: `4111 1111 1111 1111`
- Expiry: Any future date (e.g., 12/25)
- CVV: Any 3 digits (e.g., 123)

### 2. Manual Testing Checklist

- [ ] Navigate to `/donate-money.html`
- [ ] Fill in donor details
- [ ] Select or enter donation amount
- [ ] Test anonymous donation toggle
- [ ] Click "Proceed to Payment"
- [ ] Complete payment with test card
- [ ] Verify receipt generation
- [ ] Check receipt download
- [ ] Verify database entry in MoneyDonations table
- [ ] Check admin stats are updated
- [ ] Test refund functionality (admin only)

### 3. Test Different Scenarios

#### Scenario 1: Successful Donation
```javascript
// Console test
fetch('/api/donations/money/initiate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    amount: 100,
    donorName: 'Test User',
    donorEmail: 'test@example.com',
    paymentMethod: 'RAZORPAY'
  })
})
.then(r => r.json())
.then(console.log)
```

#### Scenario 2: Invalid Amount
- Try amount < ₹1 (should error)
- Try amount > ₹100000 (should error)

#### Scenario 3: Anonymous Donation
- Toggle anonymous checkbox
- Provide custom display name
- Verify donor details are hidden in receipt

---

## 📈 Database Schema

### MoneyDonations Table Columns

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary Key, Auto Increment |
| donorName | VARCHAR(255) | Required |
| donorEmail | VARCHAR(255) | Required |
| donorPhone | VARCHAR(20) | Optional |
| amount | DECIMAL(10, 2) | Min: 1.00, Validation: CHECK amount >= 1 |
| currency | ENUM('INR') | Always 'INR' |
| paymentMethod | ENUM(...) | RAZORPAY, STRIPE, PAYPAL |
| transactionId | VARCHAR(255) | Unique, Generated |
| paymentId | VARCHAR(255) | From payment gateway |
| paymentSignature | VARCHAR(255) | For verification |
| paymentStatus | ENUM(...) | PENDING, SUCCESS, FAILED, REFUNDED |
| receiptNumber | VARCHAR(255) | Unique, Generated |
| receiptDownloadUrl | TEXT | Path to PDF receipt |
| message | LONGTEXT | Optional donor message |
| isAnonymous | BOOLEAN | Default: false |
| anonymousDonorName | VARCHAR(255) | If anonymous=true |
| userId | BIGINT | FK to Users, Optional |
| receiptGeneratedAt | DATETIME | When receipt was generated |
| ipAddress | VARCHAR(45) | Donor's IP address |
| metadata | JSON | Additional data |
| createdAt | DATETIME | Auto timestamp |
| updatedAt | DATETIME | Auto timestamp |

---

## 🔐 Security Features

✅ **Payment Security**
- Razorpay signature verification
- Webhook signature verification
- Secure payment token handling

✅ **Data Protection**
- Input validation and sanitization
- SQL injection prevention (Sequelize ORM)
- XSS protection

✅ **User Privacy**
- Anonymous donation support
- Email masking for anonymous donors
- Secure receipt access control

---

## 📝 File Structure

```
CareShare/
├── config/
│   └── payment.config.js (NEW)
├── controllers/
│   ├── admin.controller.js (UPDATED)
│   └── moneyDonation.controller.js (NEW)
├── models/
│   ├── moneyDonation.model.js (NEW)
│   └── index.js (UPDATED)
├── routes/
│   ├── admin.routes.js (UPDATED)
│   ├── moneyDonation.routes.js (NEW)
│   └── server.js (UPDATED)
├── services/
│   ├── moneyDonation.service.js (NEW)
│   ├── payment.service.js (NEW)
│   └── receipt.service.js (NEW)
├── utils/
│   ├── payment-utils.js (NEW)
│   └── receipt-generator.js (NEW)
├── scripts/
│   └── migrate-money-donation.js (NEW)
├── src/main/resources/templates/
│   └── donate-money.html (NEW)
└── uploads/
    └── receipts/ (Auto-created)
```

---

## 🚀 Next Steps

1. **Configure Razorpay**
   - Add credentials to `.env` file
   - Set up webhook in Razorpay dashboard
   - Configure return URLs

2. **Customize Company Details**
   - Update company name, address, email in `.env`
   - Add company logo to `/assets/images/logo.png`
   - Update website URL

3. **Set Up Email Service**
   - Ensure email service is configured
   - Test receipt email delivery
   - Customize email templates if needed

4. **Test Complete Flow**
   - Use test credentials
   - Verify all features work
   - Test admin functionality

5. **Deploy to Production**
   - Update Razorpay credentials (live keys)
   - Enable webhook in production
   - Set HTTPS for payment endpoints
   - Configure backups for receipts

---

## 🐛 Troubleshooting

### Issue: "Cannot find module 'razorpay'"
**Solution**: Run `npm install razorpay`

### Issue: "Receipt file not found"
**Solution**: Ensure `/uploads/receipts` directory exists and is writable

### Issue: Payment verification fails
**Solution**: Check Razorpay webhook secret matches in `.env` and Razorpay dashboard

### Issue: Donation not showing in admin panel
**Solution**: Check payment status is 'SUCCESS' in database

---

## 📞 Support Resources

- **Razorpay Documentation**: https://razorpay.com/docs/
- **PDFKit Documentation**: http://pdfkit.org/
- **Sequelize Documentation**: https://sequelize.org/

---

## ✨ Features Implemented

✅ User can donate any amount starting from ₹1  
✅ Secure payment via Razorpay  
✅ Automatic PDF receipt generation  
✅ Receipt download with company details  
✅ Anonymous donation option  
✅ Admin donation management  
✅ Donation statistics dashboard  
✅ Refund functionality  
✅ Email receipt delivery  
✅ Database tracking  
✅ Error handling  
✅ Responsive UI  

---

## 📄 Document Generated
Date: April 29, 2026
Implementation Status: ✅ COMPLETE

---

Implementation completed successfully! 🎉
