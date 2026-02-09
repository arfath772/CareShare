const nodemailer = require('nodemailer');

// Check if email is properly configured
const isEmailConfigured = () => {
  return process.env.MAIL_HOST && 
         process.env.MAIL_USER && 
         process.env.MAIL_PASSWORD &&
         !process.env.MAIL_PASSWORD.includes('your_');
};

// Create transporter only if email is configured
let transporter = null;

if (isEmailConfigured()) {
  transporter = nodemailer.createTransport({
    host: process.env.MAIL_HOST,
    port: parseInt(process.env.MAIL_PORT) || 587,
    secure: false,
    auth: {
      user: process.env.MAIL_USER,
      pass: process.env.MAIL_PASSWORD
    },
    tls: {
      rejectUnauthorized: false
    },
    // Force IPv4 to avoid IPv6 timeout issues
    family: 4,
    // Shorter timeout settings to fail fast
    connectionTimeout: 5000,
    greetingTimeout: 5000,
    socketTimeout: 10000
  });

  // Verify transporter in background (non-blocking)
  setTimeout(() => {
    transporter.verify((error, success) => {
      if (error) {
        console.warn('⚠️  Email service unavailable:', error.message);
        console.warn('💡 App will continue, but password reset emails will not be sent');
      } else {
        console.log('✅ Email server is ready to send messages');
      }
    });
  }, 1000);
} else {
  console.warn('⚠️  Email not configured - Password reset feature will be disabled');
  console.warn('💡 To enable: Set MAIL_HOST, MAIL_USER, and MAIL_PASSWORD in .env');
}

// Send password reset email
const sendPasswordResetEmail = async (toEmail, resetToken, firstName) => {
  if (!transporter) {
    console.warn('⚠️  Email not configured - Cannot send password reset email');
    return false;
  }

  const resetUrl = `${process.env.APP_BASE_URL}/reset-password?token=${resetToken}`;

  const mailOptions = {
    from: process.env.MAIL_FROM || process.env.MAIL_USER,
    to: toEmail,
    subject: 'Password Reset Request - CareNShare',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2>Password Reset Request</h2>
        <p>Hi ${firstName},</p>
        <p>You requested to reset your password. Click the button below to reset it:</p>
        <div style="text-align: center; margin: 30px 0;">
          <a href="${resetUrl}" 
             style="background-color: #4CAF50; color: white; padding: 12px 30px; 
                    text-decoration: none; border-radius: 5px; display: inline-block;">
            Reset Password
          </a>
        </div>
        <p>Or copy and paste this link in your browser:</p>
        <p style="word-break: break-all; color: #666;">${resetUrl}</p>
        <p style="color: #666; font-size: 14px;">This link will expire in ${process.env.PASSWORD_RESET_EXPIRATION_MINUTES || 15} minutes.</p>
        <p style="color: #666; font-size: 14px;">If you didn't request this, please ignore this email.</p>
        <hr style="border: 0; border-top: 1px solid #eee; margin: 30px 0;">
        <p style="color: #999; font-size: 12px;">CareNShare - Share, Care, Repair</p>
      </div>
    `
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    console.log('✅ Password reset email sent:', info.messageId);
    return true;
  } catch (error) {
    console.error('❌ Error sending password reset email:', error.message);
    return false;
  }
};

// Send exchange request notifications
const sendExchangeRequestNotifications = async (exchangeRequest, ownerEmail, requesterEmail) => {
  if (!transporter) {
    console.warn('⚠️  Email not configured - Cannot send exchange notifications');
    return false;
  }

  // Email to product owner
  const ownerMailOptions = {
    from: process.env.MAIL_FROM || process.env.MAIL_USER,
    to: ownerEmail,
    subject: 'New Exchange Request - CareNShare',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2>New Exchange Request Received</h2>
        <p>You have received a new exchange request for your product:</p>
        <p><strong>Your Product:</strong> ${exchangeRequest.targetProduct.name}</p>
        <p><strong>They are offering:</strong> ${exchangeRequest.exchangeItemName}</p>
        <p><strong>Category:</strong> ${exchangeRequest.exchangeItemCategory}</p>
        <p><strong>Description:</strong> ${exchangeRequest.exchangeItemDescription}</p>
        ${exchangeRequest.additionalMessage ? `<p><strong>Message:</strong> ${exchangeRequest.additionalMessage}</p>` : ''}
        <div style="text-align: center; margin: 30px 0;">
          <a href="${process.env.APP_BASE_URL}/dashboard" 
             style="background-color: #2196F3; color: white; padding: 12px 30px; 
                    text-decoration: none; border-radius: 5px; display: inline-block;">
            View Request
          </a>
        </div>
        <p style="color: #666; font-size: 14px;">Thank you for using CareNShare!</p>
      </div>
    `
  };

  // Email to requester
  const requesterMailOptions = {
    from: process.env.MAIL_FROM || process.env.MAIL_USER,
    to: requesterEmail,
    subject: 'Exchange Request Submitted - CareNShare',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2>Exchange Request Submitted Successfully</h2>
        <p>Your exchange request has been submitted and is awaiting review.</p>
        <p><strong>Product Requested:</strong> ${exchangeRequest.targetProduct.name}</p>
        <p><strong>Your Offering:</strong> ${exchangeRequest.exchangeItemName}</p>
        <p>The product owner will review your request and respond soon.</p>
        <div style="text-align: center; margin: 30px 0;">
          <a href="${process.env.APP_BASE_URL}/dashboard" 
             style="background-color: #4CAF50; color: white; padding: 12px 30px; 
                    text-decoration: none; border-radius: 5px; display: inline-block;">
            View My Requests
          </a>
        </div>
        <p style="color: #666; font-size: 14px;">Thank you for using CareNShare!</p>
      </div>
    `
  };

  try {
    await Promise.all([
      transporter.sendMail(ownerMailOptions),
      transporter.sendMail(requesterMailOptions)
    ]);
    console.log('✅ Exchange request notification emails sent');
    return true;
  } catch (error) {
    console.error('❌ Error sending exchange request notifications:', error.message);
    return false;
  }
};

// Send exchange status update
const sendExchangeStatusUpdate = async (exchangeRequest, ownerEmail, requesterEmail) => {
  if (!transporter) {
    console.warn('⚠️  Email not configured - Cannot send status update');
    return false;
  }

  const status = exchangeRequest.status;
  const statusText = status === 'APPROVED' ? 'Approved' : 'Rejected';
  const statusColor = status === 'APPROVED' ? '#4CAF50' : '#f44336';

  const requesterMailOptions = {
    from: process.env.MAIL_FROM,
    to: requesterEmail,
    subject: `Exchange Request ${statusText} - CareNShare`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: ${statusColor};">Exchange Request ${statusText}</h2>
        <p>Your exchange request has been ${status.toLowerCase()}.</p>
        <p><strong>Product:</strong> ${exchangeRequest.targetProduct.name}</p>
        <p><strong>Your Offering:</strong> ${exchangeRequest.exchangeItemName}</p>
        ${status === 'REJECTED' && exchangeRequest.rejectionReason ? 
          `<p><strong>Reason:</strong> ${exchangeRequest.rejectionReason}</p>` : ''}
        ${status === 'APPROVED' ? 
          `<p style="color: #4CAF50;">Congratulations! The owner has accepted your exchange offer. Please check your dashboard for further details.</p>` : ''}
        <div style="text-align: center; margin: 30px 0;">
          <a href="${process.env.APP_BASE_URL}/dashboard" 
             style="background-color: ${statusColor}; color: white; padding: 12px 30px; 
                    text-decoration: none; border-radius: 5px; display: inline-block;">
            View Details
          </a>
        </div>
      </div>
    `
  };

  try {
    await transporter.sendMail(requesterMailOptions);
    console.log('✅ Exchange status update email sent');
    return true;
  } catch (error) {
    console.error('❌ Error sending exchange status update:', error.message);
    return false;
  }
};

module.exports = {
  sendPasswordResetEmail,
  sendExchangeRequestNotifications,
  sendExchangeStatusUpdate
};
