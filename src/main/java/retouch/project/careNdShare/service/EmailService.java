package retouch.project.careNdShare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import retouch.project.careNdShare.entity.PurchaseRequest;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetLink = baseUrl + "/reset-password?token=" + resetToken;

            String subject = "Care & Share - Password Reset Request";
            String htmlContent = buildPasswordResetEmail(firstName, resetLink);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send purchase notifications to both buyer and seller
     */
    public void sendPurchaseNotifications(PurchaseRequest purchase) {
        try {
            Product product = purchase.getProduct();
            User seller = product.getUser();

            if (product == null || seller == null) {
                System.err.println("❌ Cannot send emails: Product or Seller is null");
                return;
            }

            // Send email to buyer
            sendBuyerConfirmation(purchase);

            // Send email to seller
            sendSellerNotification(purchase);

            System.out.println("✅ Email notifications sent successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed to send email notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send confirmation email to buyer
     */
    private void sendBuyerConfirmation(PurchaseRequest purchase) {
        try {
            Product product = purchase.getProduct();
            User seller = product.getUser();

            String subject = "Purchase Confirmation - Order #" + purchase.getId();
            String htmlContent = buildBuyerConfirmationEmail(purchase, product, seller);

            sendHtmlEmail(purchase.getEmail(), subject, htmlContent);
            System.out.println("✅ Buyer confirmation email sent to: " + purchase.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send buyer email: " + e.getMessage());
        }
    }

    /**
     * Send notification email to seller
     */
    private void sendSellerNotification(PurchaseRequest purchase) {
        try {
            Product product = purchase.getProduct();
            User seller = product.getUser();

            String subject = "Congratulations! Your Item Sold - " + product.getName();
            String htmlContent = buildSellerNotificationEmail(purchase, product, seller);

            sendHtmlEmail(seller.getEmail(), subject, htmlContent);
            System.out.println("✅ Seller notification email sent to: " + seller.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send seller email: " + e.getMessage());
        }
    }

    /**
     * Core HTML email sending method
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("📧 HTML email successfully sent to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }

    // ADDED METHODS START HERE

    /**
     * Simple email sending method for plain text emails
     */
    public void sendEmail(String to, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, false); // false indicates plain text

            mailSender.send(mimeMessage);
            System.out.println("📧 Plain text email sent to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Failed to send plain text email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }

    /**
     * Send status update email to buyer
     */
    public void sendStatusUpdateEmail(PurchaseRequest purchase) {
        try {
            String subject = "Order Status Update - " + purchase.getProduct().getName();
            String htmlContent = buildStatusUpdateEmail(purchase);

            sendHtmlEmail(purchase.getBuyer().getEmail(), subject, htmlContent);
            System.out.println("✅ Status update email sent to buyer: " + purchase.getBuyer().getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send status update email: " + e.getMessage());
        }
    }

    /**
     * Build status update email HTML content
     */
    private String buildStatusUpdateEmail(PurchaseRequest purchase) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("    <meta charset=\"UTF-8\">");
        html.append("    <style>");
        html.append("        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }");
        html.append("        .container { max-width: 600px; margin: 0 auto; background: #ffffff; }");
        html.append("        .header { background: linear-gradient(135deg, #007bff, #0056b3); padding: 30px; text-align: center; color: white; }");
        html.append("        .content { padding: 30px; background: #f8f9fa; }");
        html.append("        .order-details { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #007bff; }");
        html.append("        .status-badge { display: inline-block; padding: 5px 15px; background: #17a2b8; color: white; border-radius: 20px; font-weight: bold; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>📦 Order Status Updated</h1>");
        html.append("            <p>Your order status has been updated</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(purchase.getBuyer().getFirstName()).append("!</h2>");
        html.append("            <p>Your order status has been updated. Here are the latest details:</p>");
        html.append("            ");
        html.append("            <div class=\"order-details\">");
        html.append("                <h3>Order Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Order ID:</span> #").append(purchase.getId()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Product:</span> ").append(purchase.getProduct().getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Seller:</span> ").append(purchase.getProduct().getUser().getFirstName()).append(" ").append(purchase.getProduct().getUser().getLastName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">New Status:</span> <span class=\"status-badge\">").append(purchase.getStatus()).append("</span></div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Last Updated:</span> ").append(purchase.getUpdatedAt() != null ? purchase.getUpdatedAt() : purchase.getCreatedAt()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div style=\"background: #e7f3ff; padding: 20px; border-radius: 10px; margin: 20px 0;\">");
        html.append("                <h3>ℹ️ What's Next?</h3>");
        html.append("                <p>If you have any questions about your order status, please contact the seller directly.</p>");
        html.append("                <p>Seller Email: ").append(purchase.getProduct().getUser().getEmail()).append("</p>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <p>Thank you for choosing <strong>Care & Share</strong>!</p>");
        html.append("        </div>");
        html.append("        <div class=\"footer\">");
        html.append("            <p>&copy; 2025 Care & Share. All rights reserved.</p>");
        html.append("            <p>This is an automated email, please do not reply.</p>");
        html.append("        </div>");
        html.append("    </div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    // ADDED METHODS END HERE

    /**
     * Build buyer confirmation email HTML content
     */
    private String buildBuyerConfirmationEmail(PurchaseRequest purchase, Product product, User seller) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("    <meta charset=\"UTF-8\">");
        html.append("    <style>");
        html.append("        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }");
        html.append("        .container { max-width: 600px; margin: 0 auto; background: #ffffff; }");
        html.append("        .header { background: linear-gradient(135deg, #28a745, #218838); padding: 30px; text-align: center; color: white; }");
        html.append("        .content { padding: 30px; background: #f8f9fa; }");
        html.append("        .order-details { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .seller-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #28a745; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🎉 Purchase Confirmed!</h1>");
        html.append("            <p>Thank you for your order</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(purchase.getFullName()).append("!</h2>");
        html.append("            <p>Your purchase has been successfully confirmed on Care & Share.</p>");
        html.append("            ");
        html.append("            <div class=\"order-details\">");
        html.append("                <h3>📦 Order Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Order ID:</span> #").append(purchase.getId()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Product:</span> ").append(product.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Price:</span> ₹").append(String.format("%.2f", product.getPrice())).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Payment Method:</span> ").append(purchase.getPaymentMethod()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Purchase Date:</span> ").append(purchase.getCreatedAt()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"seller-info\">");
        html.append("                <h3>👤 Seller Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Name:</span> ").append(seller.getFirstName()).append(" ").append(seller.getLastName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Email:</span> ").append(seller.getEmail()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div style=\"background: #e7f3ff; padding: 20px; border-radius: 10px; margin: 20px 0;\">");
        html.append("                <h3>📞 Next Steps</h3>");
        html.append("                <p>Please contact the seller within 24 hours to arrange pickup or delivery.</p>");
        html.append("                <p>Keep your order ID (#").append(purchase.getId()).append(") handy for reference.</p>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <p>Thank you for choosing <strong>Care & Share</strong>!</p>");
        html.append("        </div>");
        html.append("        <div class=\"footer\">");
        html.append("            <p>&copy; 2025 Care & Share. All rights reserved.</p>");
        html.append("            <p>This is an automated email, please do not reply.</p>");
        html.append("        </div>");
        html.append("    </div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Build seller notification email HTML content
     */
    private String buildSellerNotificationEmail(PurchaseRequest purchase, Product product, User seller) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("    <meta charset=\"UTF-8\">");
        html.append("    <style>");
        html.append("        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }");
        html.append("        .container { max-width: 600px; margin: 0 auto; background: #ffffff; }");
        html.append("        .header { background: linear-gradient(135deg, #ff6b35, #f7931e); padding: 30px; text-align: center; color: white; }");
        html.append("        .content { padding: 30px; background: #f8f9fa; }");
        html.append("        .sale-details { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .buyer-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #ff6b35; }");
        html.append("        .highlight { background: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>💰 Your Item Sold!</h1>");
        html.append("            <p>Congratulations on your sale!</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(seller.getFirstName()).append("!</h2>");
        html.append("            <p>Great news! Your item has been sold on Care & Share.</p>");
        html.append("            ");
        html.append("            <div class=\"sale-details\">");
        html.append("                <h3>🎯 Sale Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Order ID:</span> #").append(purchase.getId()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Item:</span> ").append(product.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Sale Price:</span> ₹").append(String.format("%.2f", product.getPrice())).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Payment Method:</span> ").append(purchase.getPaymentMethod()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Sale Date:</span> ").append(purchase.getCreatedAt()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"buyer-info\">");
        html.append("                <h3>👤 Buyer Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Name:</span> ").append(purchase.getFullName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Email:</span> ").append(purchase.getEmail()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Phone:</span> ").append(purchase.getPhone()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Shipping Address:</span> ").append(purchase.getShippingAddress()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"highlight\">");
        html.append("                <h3>📞 Next Steps</h3>");
        html.append("                <p>Please contact the buyer within 24 hours to arrange the transaction.</p>");
        html.append("                <p><strong>Order ID #").append(purchase.getId()).append("</strong> - Use this for reference when communicating with the buyer.</p>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <p>Thank you for using <strong>Care & Share</strong>!</p>");
        html.append("        </div>");
        html.append("        <div class=\"footer\">");
        html.append("            <p>&copy; 2025 Care & Share. All rights reserved.</p>");
        html.append("            <p>This is an automated email, please do not reply.</p>");
        html.append("        </div>");
        html.append("    </div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String buildPasswordResetEmail(String firstName, String resetLink) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("    <meta charset=\"UTF-8\">");
        html.append("    <style>");
        html.append("        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append("        .header { background: linear-gradient(135deg, #28a745, #218838); padding: 20px; text-align: center; color: white; border-radius: 10px 10px 0 0; }");
        html.append("        .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }");
        html.append("        .button { display: inline-block; padding: 12px 30px; background: #28a745; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append("        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>Care & Share</h1>");
        html.append("            <p>Password Reset Request</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(firstName).append("!</h2>");
        html.append("            <p>You requested to reset your password for your Care & Share account.</p>");
        html.append("            <p>Click the button below to reset your password. This link will expire in 30 minutes.</p>");
        html.append("            <p style=\"text-align: center;\">");
        html.append("                <a href=\"").append(resetLink).append("\" class=\"button\">Reset Password</a>");
        html.append("            </p>");
        html.append("            <p>If the button doesn't work, copy and paste this link in your browser:</p>");
        html.append("            <p style=\"word-break: break-all; background: #e9ecef; padding: 10px; border-radius: 5px;\">");
        html.append("                ").append(resetLink);
        html.append("            </p>");
        html.append("            <p>If you didn't request this password reset, please ignore this email.</p>");
        html.append("        </div>");
        html.append("        <div class=\"footer\">");
        html.append("            <p>&copy; 2025 Care & Share. All rights reserved.</p>");
        html.append("        </div>");
        html.append("    </div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}