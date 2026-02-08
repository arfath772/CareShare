# CareShare Application - Complete Feature Explanation

## 🎯 Overview
**CareShare** is a **Spring Boot 3.5.6** web application for **donation, exchange, and resale** of items. It connects people who want to donate or sell items with those who need them, promoting reuse and community sharing.

---

## 🏗️ Technical Architecture

### Technology Stack:
- **Backend**: Spring Boot 3.5.6, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf templates, HTML, CSS, JavaScript
- **Database**: MySQL (zero_db)
- **Authentication**: JWT (JSON Web Tokens) with HTTP-only cookies
- **Email**: Spring Mail (Gmail SMTP)
- **File Storage**: Local file system (`/uploads/`)
- **Build Tool**: Maven

### Security Configuration:
- JWT-based authentication
- BCrypt password encryption
- Role-based access control (USER, ADMIN)
- Session stateless (REST API)
- CSRF disabled for API endpoints

---

## 👥 User Management System

### 1. User Registration (`/api/auth/register`)
**What happens:**
1. User fills registration form (email, password, firstName, lastName)
2. System validates email uniqueness
3. Password is hashed using BCrypt
4. Default role "USER" is assigned
5. User account created in database
6. Success response returned

**Entities Involved:** `User`

### 2. User Login (`/api/auth/login`)
**What happens:**
1. User submits email + password
2. Spring Security authenticates credentials
3. JWT token generated (valid 24 hours)
4. Token stored in HTTP-only cookie
5. User details returned (email, firstName, lastName)
6. User redirected to dashboard

**Flow:**
```
Login → Authenticate → Generate JWT → Set Cookie → Dashboard
```

### 3. Password Reset Flow

#### Forgot Password (`/api/auth/forgot-password`)
**What happens:**
1. User enters email address
2. System generates unique reset token (UUID)
3. Token expires in 30 minutes
4. Token saved to user record
5. Email sent with reset link: `http://localhost:8080/reset-password?token=XXX`
6. Generic success message (security: doesn't reveal if email exists)

#### Reset Password (`/api/auth/reset-password`)
**What happens:**
1. User clicks email link
2. System validates token (not expired, exists)
3. User enters new password (validated against confirm password)
4. Password updated with BCrypt hash
5. Reset token cleared from database
6. User can log in with new password

### 4. Logout (`/api/auth/logout`)
**What happens:**
1. JWT cookie deleted (max-age=0)
2. User session cleared
3. Redirect to login page

---

## 📦 Product Management System

### Product Types:
1. **Donate** - Free items
2. **Exchange** - Swap items
3. **Resell** - Sell items for money

### Product Statuses:
- `PENDING` - Awaiting admin approval
- `APPROVED` - Visible to public
- `REJECTED` - Admin rejected
- `SOLD` - Already purchased

### 1. Add Product (`/api/products/add`)
**What happens:**
1. User fills product form:
   - Name, price, category (old/new), type (Donate/Exchange/Resell)
   - Description, condition (new/fair/good/excellent)
   - Upload images (max 10MB total)
2. Images saved to `/uploads/product_{id}/`
3. Product status set to `PENDING`
4. Product linked to current user
5. Awaits admin approval

**Database:** `Product` entity created with relationships to `User`

### 2. My Products (`/api/products/my-products`)
**What happens:**
1. Fetches all products created by current user
2. Returns product list with status (PENDING, APPROVED, REJECTED, SOLD)
3. Shows images, descriptions, prices

### 3. Available Products (`/api/products/available`)
**What happens:**
1. Returns only `APPROVED` products
2. Filters by type (optional): Donate, Exchange, Resell
3. Excludes user's own products
4. Public marketplace view

### 4. Product Details (`/api/products/{id}`)
**What happens:**
1. Fetches complete product information
2. Includes seller details
3. Shows all uploaded images
4. Used for detailed view before purchase/exchange/request

---

## 🎁 Donation System

### Entities:
- `DonateItem` - Items donated by users
- `DonateRequest` - Requests to receive donations
- **Statuses:** `PENDING`, `APPROVED`, `REJECTED`, `CLAIMED`

### 1. Donate Item (`/api/donate/add`)
**What happens:**
1. User fills donation form:
   - Item type (book, clothes, toy, other)
   - Item name, quantity, condition
   - Pickup address
   - Upload multiple images
2. Images saved to `/uploads/donations/{id}/`
3. Main image + array of images stored
4. Status set to `PENDING`
5. Awaits admin approval

### 2. Browse Available Donations (`/api/donate/available`)
**What happens:**
1. Shows only `APPROVED` donations
2. Filters by item type (optional)
3. Excludes user's own donations
4. Displays as browsable catalog

### 3. Request Donation (`/api/donate/request`)
**What happens:**
1. User selects donation item
2. Fills request form with description
3. `DonateRequest` created with status `PENDING`
4. Links requester (user), donateItem, and donor
5. Request awaits donor approval

### 4. Donor Approves/Rejects Request
**What happens (Approve):**
1. Donor sees incoming requests
2. Donor approves request
3. DonateRequest status → `APPROVED`
4. DonateItem status → `CLAIMED`
5. Item no longer available to others

**What happens (Reject):**
1. Donor rejects with reason
2. DonateRequest status → `REJECTED`
3. Item remains available

### 5. My Donations (`/api/donate/my-donations`)
**What happens:**
1. Shows all items current user donated
2. Displays status and incoming requests

### 6. My Requests (`/api/donate/my-requests`)
**What happens:**
1. Shows all donation requests user made
2. Displays status (PENDING, APPROVED, REJECTED)

---

## 🔄 Exchange System

### Entities:
- `ExchangeRequest` - Proposal to exchange items
- **Statuses:** `PENDING`, `APPROVED`, `REJECTED`

### 1. Submit Exchange Request (`/api/exchange-requests/submit`)
**What happens:**
1. User finds product they want (target product)
2. User offers their own item in exchange:
   - Exchange item name, category, description
   - Upload images of item to offer (max 12 images)
   - Additional message to owner
3. Images saved to `/uploads/exchange-items/`
4. ExchangeRequest created with status `PENDING`
5. **Email notifications sent:**
   - To product owner: "Someone wants to exchange for your item"
   - To requester: "Your exchange request was submitted"

### 2. View My Exchange Requests (`/api/exchange-requests/my-requests`)
**What happens:**
1. Shows exchanges user initiated
2. Filters by status (optional)
3. Displays target product + offered item details

### 3. View Received Exchange Requests (`/api/exchange-requests/received`)
**What happens:**
1. Shows exchanges others proposed for user's products
2. User can see offered items with images
3. Can approve or decline

### 4. Accept Exchange (`/api/exchange-requests/{id}/accept`)
**What happens:**
1. Owner approves exchange request
2. Status changes to `APPROVED`
3. **Email sent to both parties:**
   - Owner: "You accepted the exchange"
   - Requester: "Your exchange was accepted! Contact details provided"
4. Both parties coordinate offline

### 5. Decline Exchange (`/api/exchange-requests/{id}/decline`)
**What happens:**
1. Owner declines with reason
2. Status changes to `REJECTED`
3. **Email sent to requester** with rejection reason

---

## 💰 Purchase System

### Entities:
- `PurchaseRequest` - Buy request for resell items
- **Statuses:** `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`

### 1. Create Purchase (`/api/purchases/create`)
**What happens:**
1. Buyer selects product to purchase
2. Fills purchase form:
   - Full name, email, phone
   - Shipping address
   - Payment method (UPI, CARD, NETBANKING, COD)
   - Amount (locked to product price)
3. PurchaseRequest created with status `PENDING`
4. Linked to buyer and product
5. Order confirmation shown

**Note:** Payment is NOT processed (mock system), only order tracking.

### 2. My Purchases (`/api/purchases/my-purchases`)
**What happens:**
1. Shows all products current user bought
2. Displays order status
3. Shows shipping address, payment method

### 3. My Sales (`/api/purchases/my-sales`)
**What happens:**
1. Shows all products current user sold
2. Displays buyer information
3. Shows order status

### 4. Update Purchase Status (`/api/purchases/{id}/status`)
**What happens:**
1. Seller updates order status:
   - `CONFIRMED` - Order confirmed
   - `SHIPPED` - Order shipped
   - `DELIVERED` - Order delivered
   - `CANCELLED` - Order cancelled
2. Status tracked throughout lifecycle
3. Both buyer and seller can view status

---

## 👑 Admin Management System

**Access:** Requires `ROLE_ADMIN` (protected by `@PreAuthorize`)

### 1. Admin Dashboard (`/admin`)
**What happens:**
1. Shows admin statistics:
   - Total users, admin users, regular users
   - Pending/approved/rejected products
   - Pending/approved/rejected donations
   - Pending/approved/rejected exchanges

### 2. User Management (`/api/admin/users`)

#### View All Users:
- Lists all registered users
- Shows email, name, roles, admin status

#### Update User Role (`/api/admin/users/{id}/role`):
**What happens:**
1. Admin toggles user's admin status
2. Role changed between USER and ADMIN
3. User gains/loses admin privileges

#### Delete User (`/api/admin/users/{id}`):
**What happens:**
1. Admin deletes user account
2. Cascade deletes user's products, donations, requests

### 3. Product Management

#### View Pending Products (`/api/admin/products/pending`):
- Shows all products awaiting approval

#### Approve Product (`/api/admin/products/{id}/approve`):
**What happens:**
1. Admin approves product
2. Status → `APPROVED`
3. Product becomes visible in marketplace
4. Approval timestamp recorded

#### Reject Product (`/api/admin/products/{id}/reject`):
**What happens:**
1. Admin rejects with reason
2. Status → `REJECTED`
3. Rejection reason stored
4. User can see why product was rejected

### 4. Donation Management

#### Approve Donation (`/api/admin/donations/{id}/approve`):
**What happens:**
1. Status → `APPROVED`
2. Donation visible in browse page

#### Reject Donation (`/api/admin/donations/{id}/reject`):
**What happens:**
1. Status → `REJECTED`
2. Reason provided to donor

### 5. Exchange Request Management

#### View All Exchanges (`/api/admin/exchange-requests`):
- Lists all exchange requests (all statuses)

#### Approve Exchange (`/api/admin/exchange-requests/{id}/approve`):
**What happens:**
1. Status → `APPROVED`
2. Emails sent to both parties

#### Reject Exchange (`/api/admin/exchange-requests/{id}/reject`):
**What happens:**
1. Status → `REJECTED`
2. Rejection reason required
3. Email sent to requester

---

## 📧 Email Notification System

Using **Spring Mail** with **Gmail SMTP:**

### Emails Sent:

1. **Password Reset Email:**
   - Sent when user requests password reset
   - Contains reset link with token
   - Expires in 30 minutes

2. **Exchange Request Notifications:**
   - **To Owner:** "Someone wants to exchange with you"
   - **To Requester:** "Your request was submitted"

3. **Exchange Status Updates:**
   - **Approved:** Contact details shared
   - **Rejected:** Reason provided

**Configuration:**
- SMTP: smtp.gmail.com:587
- TLS enabled
- App password authentication

---

## 🔐 Security Features

### 1. JWT Authentication:
- Token valid 24 hours
- Stored in HTTP-only cookie (XSS protection)
- Verified on every API request
- Token includes: email, roles, expiration

### 2. Password Security:
- BCrypt hashing (strength 10)
- Never sent in responses (`@JsonIgnore`)
- Reset tokens are UUIDs
- Token expiration enforced

### 3. Authorization:
- **Public:** `/`, `/login`, `/register`, `/forgot-password`, `/reset-password`
- **User:** `/dashboard`, `/api/products`, `/api/donate`, `/api/purchases`, `/api/exchange-requests`
- **Admin:** `/admin`, `/api/admin/**`

### 4. CORS & CSRF:
- CSRF disabled (stateless API)
- CORS configured for specific endpoints

---

## 📂 File Upload System

### Storage Structure:
```
uploads/
├── product_{id}/
│   ├── image1.jpg
│   ├── image2.jpg
├── donations/{id}/
│   ├── image1.jpg
├── exchange-items/
│   ├── {requestId}_image1.jpg
```

### Validation:
- Max file size: 10MB (configured in application.properties)
- Allowed types: JPEG, PNG, GIF
- Multiple images supported
- Unique filenames (UUID + original name)

---

## 🎨 Frontend Pages

### Public Pages:
- `index.html` - Home page
- `login.html` - Login form
- `register.html` - Registration form
- `forgot-password.html` - Request reset
- `reset-password.html` - Reset password form

### User Pages:
- `dashboard.html` - User dashboard
- `donate.html` - Donation overview
- `donate-now.html` - Create donation
- `donate-items.html` - My donations
- `browse-donations.html` - Browse available donations
- `request-now.html` - Request donation
- `why-donation.html` - Information page

### Admin Pages:
- `admin.html` - Admin dashboard

---

## 🔄 Complete User Workflows

### Workflow 1: Donate Item
```
User Login → Dashboard → Donate Now → Fill Form → Upload Images 
→ Submit → Admin Reviews → Approve → Item Visible → Someone Requests 
→ Donor Approves → Item Claimed
```

### Workflow 2: Purchase Item
```
User Login → Browse Products → Select Resell Item → View Details 
→ Click Buy → Fill Purchase Form → Submit → Seller Confirms 
→ Ships → Delivered
```

### Workflow 3: Exchange Item
```
User Login → Browse Products → Select Exchange Item → Submit Exchange 
→ Upload Your Item Photos → Owner Reviews → Approves → Email Exchange 
→ Coordinate Offline
```

### Workflow 4: Forgot Password
```
Forgot Password Page → Enter Email → Check Email → Click Reset Link 
→ Enter New Password → Login with New Password
```

---

## 📊 Database Schema

### Main Tables:
1. **users** - User accounts, passwords, roles
2. **products** - All products (donate, exchange, resell)
3. **donate_items** - Donation items
4. **donate_requests** - Donation requests
5. **exchange_requests** - Exchange proposals
6. **purchases** - Purchase orders
7. **product_images** - Multiple product images
8. **exchange_request_images** - Multiple exchange images

---

## 🚀 When Application Starts

1. Spring Boot initializes
2. MySQL connection established (HikariCP pool)
3. JPA scans entities, creates/updates tables
4. Security filters configured
5. JWT secret loaded
6. Email service configured
7. Tomcat starts on port **8080**
8. Application ready at `http://localhost:8080`

---

## 💡 Key Features Summary

✅ **User Authentication** - JWT-based secure login  
✅ **Password Reset** - Email-based reset flow  
✅ **Multi-Type Products** - Donate, Exchange, Resell  
✅ **Admin Approval** - All items reviewed before public  
✅ **File Uploads** - Multiple images per item  
✅ **Email Notifications** - Status updates via email  
✅ **Purchase Tracking** - Order status management  
✅ **Exchange System** - Barter items with photos  
✅ **Donation Requests** - Request-approve workflow  
✅ **Role-Based Access** - USER vs ADMIN permissions  
✅ **Responsive Design** - Thymeleaf templates  

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- MySQL 8.0+
- Maven 3.6+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/arfath772/CareShare.git
   cd CareShare
   ```

2. **Create MySQL Database**
   ```sql
   CREATE DATABASE zero_db;
   ```

3. **Configure Environment Variables**
   
   Set the following environment variables (see [SECURITY_SETUP.md](SECURITY_SETUP.md) for detailed instructions):
   
   ```bash
   # Windows PowerShell
   $env:DB_PASSWORD="your_database_password"
   $env:JWT_SECRET="your_jwt_secret_key"
   $env:MAIL_PASSWORD="your_gmail_app_password"
   ```

4. **Run the application**
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

5. **Access the application**
   - URL: `http://localhost:8080`
   - Create an account or login

---

## 📝 License

This project is licensed under the MIT License.

---

## 👥 Contributors

- **Arfath** - [GitHub](https://github.com/arfath772)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📧 Contact

For any queries, please reach out via GitHub issues or email.

---

**CareShare** - *Promoting sustainable sharing and community support* 🌱
