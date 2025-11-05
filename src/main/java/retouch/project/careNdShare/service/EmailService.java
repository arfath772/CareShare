package retouch.project.careNdShare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import retouch.project.careNdShare.entity.PurchaseRequest;
import retouch.project.careNdShare.entity.ExchangeRequest;
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

    // EXCHANGE REQUEST METHODS START HERE

    /**
     * Send exchange request notifications to both item owner and requester
     */
    public void sendExchangeRequestNotifications(ExchangeRequest exchangeRequest, String ownerEmail, String requesterEmail) {
        try {
            Product targetProduct = exchangeRequest.getTargetProduct();
            User owner = targetProduct.getUser();
            User requester = exchangeRequest.getRequester();

            if (ownerEmail == null || ownerEmail.trim().isEmpty() || requesterEmail == null || requesterEmail.trim().isEmpty()) {
                System.err.println("❌ Cannot send exchange emails: Email addresses are null or empty");
                return;
            }

            // Send email to item owner
            sendExchangeRequestToOwner(exchangeRequest, targetProduct, owner, requester, ownerEmail);

            // Send email to requester
            sendExchangeRequestToRequester(exchangeRequest, targetProduct, owner, requester, requesterEmail);

            System.out.println("✅ Exchange request email notifications sent successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed to send exchange request notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send exchange status update notifications to both users
     */
    public void sendExchangeStatusUpdate(ExchangeRequest exchangeRequest, String ownerEmail, String requesterEmail) {
        try {
            Product targetProduct = exchangeRequest.getTargetProduct();
            User owner = targetProduct.getUser();
            User requester = exchangeRequest.getRequester();

            // Send status update to owner
            sendExchangeStatusUpdateToOwner(exchangeRequest, targetProduct, owner, requester, ownerEmail);

            // Send status update to requester
            sendExchangeStatusUpdateToRequester(exchangeRequest, targetProduct, owner, requester, requesterEmail);

            System.out.println("✅ Exchange status update email notifications sent successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed to send exchange status update notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send exchange request notification to item owner with specific email
     */
    private void sendExchangeRequestToOwner(ExchangeRequest exchangeRequest, Product targetProduct,
                                            User owner, User requester, String ownerEmail) {
        try {
            String subject = "New Exchange Request for Your Item - " + targetProduct.getName();
            String htmlContent = buildExchangeRequestToOwnerEmail(exchangeRequest, targetProduct, owner, requester);

            sendHtmlEmail(ownerEmail, subject, htmlContent);
            System.out.println("✅ Exchange request email sent to owner: " + ownerEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send exchange request to owner: " + e.getMessage());
        }
    }

    /**
     * Send exchange request notification to requester with specific email
     */
    private void sendExchangeRequestToRequester(ExchangeRequest exchangeRequest, Product targetProduct,
                                                User owner, User requester, String requesterEmail) {
        try {
            String subject = "Exchange Request Submitted - " + targetProduct.getName();
            String htmlContent = buildExchangeRequestToRequesterEmail(exchangeRequest, targetProduct, owner, requester);

            sendHtmlEmail(requesterEmail, subject, htmlContent);
            System.out.println("✅ Exchange request email sent to requester: " + requesterEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send exchange request to requester: " + e.getMessage());
        }
    }

    /**
     * Send exchange status update to item owner with specific email
     */
    private void sendExchangeStatusUpdateToOwner(ExchangeRequest exchangeRequest, Product targetProduct,
                                                 User owner, User requester, String ownerEmail) {
        try {
            String subject = "Exchange Request Update - " + targetProduct.getName();
            String htmlContent = buildExchangeStatusUpdateToOwnerEmail(exchangeRequest, targetProduct, owner, requester);

            sendHtmlEmail(ownerEmail, subject, htmlContent);
            System.out.println("✅ Exchange status update email sent to owner: " + ownerEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send exchange status update to owner: " + e.getMessage());
        }
    }

    /**
     * Send exchange status update to requester with specific email
     */
    private void sendExchangeStatusUpdateToRequester(ExchangeRequest exchangeRequest, Product targetProduct,
                                                     User owner, User requester, String requesterEmail) {
        try {
            String subject = "Exchange Request Update - " + targetProduct.getName();
            String htmlContent = buildExchangeStatusUpdateToRequesterEmail(exchangeRequest, targetProduct, owner, requester);

            sendHtmlEmail(requesterEmail, subject, htmlContent);
            System.out.println("✅ Exchange status update email sent to requester: " + requesterEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send exchange status update to requester: " + e.getMessage());
        }
    }

    // EXCHANGE REQUEST METHODS END HERE

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

    // EXCHANGE EMAIL TEMPLATES START HERE

    /**
     * Build exchange request email to owner HTML content
     */
    private String buildExchangeRequestToOwnerEmail(ExchangeRequest exchangeRequest, Product targetProduct,
                                                    User owner, User requester) {
        String acceptLink = baseUrl + "/api/exchange/" + exchangeRequest.getId() + "/accept";
        String declineLink = baseUrl + "/api/exchange/" + exchangeRequest.getId() + "/decline";
        String chatLink = baseUrl + "/chat?user=" + requester.getId();

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
        html.append("        .item-details { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .user-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #007bff; }");
        html.append("        .button-container { text-align: center; margin: 30px 0; }");
        html.append("        .button { display: inline-block; padding: 12px 30px; margin: 0 10px; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append("        .accept-btn { background: #28a745; }");
        html.append("        .decline-btn { background: #dc3545; }");
        html.append("        .chat-btn { background: #17a2b8; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🔄 New Exchange Request</h1>");
        html.append("            <p>Someone wants to exchange with your item</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(owner.getFirstName()).append("!</h2>");
        html.append("            <p>You have received a new exchange request for your item.</p>");
        html.append("            ");
        html.append("            <div class=\"item-details\">");
        html.append("                <h3>📦 Your Item Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Item Name:</span> ").append(targetProduct.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Category:</span> ").append(targetProduct.getCategory()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Description:</span> ").append(targetProduct.getDescription()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"item-details\">");
        html.append("                <h3>🔄 Offered Item Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Item Name:</span> ").append(exchangeRequest.getExchangeItemName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Category:</span> ").append(exchangeRequest.getExchangeItemCategory()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Description:</span> ").append(exchangeRequest.getExchangeItemDescription()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Additional Message:</span> ").append(exchangeRequest.getAdditionalMessage() != null ? exchangeRequest.getAdditionalMessage() : "No additional message").append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"user-info\">");
        html.append("                <h3>👤 Requester Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Name:</span> ").append(requester.getFirstName()).append(" ").append(requester.getLastName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Email:</span> ").append(requester.getEmail()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"button-container\">");
        html.append("                <a href=\"").append(acceptLink).append("\" class=\"button accept-btn\">✅ Accept Request</a>");
        html.append("                <a href=\"").append(declineLink).append("\" class=\"button decline-btn\">❌ Decline Request</a>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div style=\"background: #e7f3ff; padding: 20px; border-radius: 10px; margin: 20px 0;\">");
        html.append("                <h3>💬 Chat Feature</h3>");
        html.append("                <p>If you accept this request, you can chat with the requester directly from our website.</p>");
        html.append("                <p style=\"text-align: center;\">");
        html.append("                    <a href=\"").append(chatLink).append("\" class=\"button chat-btn\">Open Chat</a>");
        html.append("                </p>");
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

    /**
     * Build exchange request email to requester HTML content
     */
    private String buildExchangeRequestToRequesterEmail(ExchangeRequest exchangeRequest, Product targetProduct,
                                                        User owner, User requester) {
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
        html.append("        .item-details { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .user-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #28a745; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🔄 Exchange Request Submitted</h1>");
        html.append("            <p>Your exchange request has been received</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(requester.getFirstName()).append("!</h2>");
        html.append("            <p>Your exchange request has been successfully submitted and it has been approved.</p>");
        html.append("            ");
        html.append("            <div class=\"item-details\">");
        html.append("                <h3>📦 Requested Item Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Item Name:</span> ").append(targetProduct.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Category:</span> ").append(targetProduct.getCategory()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Description:</span> ").append(targetProduct.getDescription()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Owner:</span> ").append(owner.getFirstName()).append(" ").append(owner.getLastName()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"item-details\">");
        html.append("                <h3>🔄 Your Offered Item</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Item Name:</span> ").append(exchangeRequest.getExchangeItemName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Category:</span> ").append(exchangeRequest.getExchangeItemCategory()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Description:</span> ").append(exchangeRequest.getExchangeItemDescription()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Additional Message:</span> ").append(exchangeRequest.getAdditionalMessage() != null ? exchangeRequest.getAdditionalMessage() : "No additional message").append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div style=\"background: #e7f3ff; padding: 20px; border-radius: 10px; margin: 20px 0;\">");
        html.append("                <h3>⏳ Next Steps</h3>");
        html.append("                <p>The item owner will review your request and respond soon.</p>");
        html.append("                <p>If the owner accepts your request, you will be able to chat with them directly from our website to arrange the exchange.</p>");
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

    /**
     * Build exchange status update email to owner HTML content
     */
    private String buildExchangeStatusUpdateToOwnerEmail(ExchangeRequest exchangeRequest, Product targetProduct,
                                                         User owner, User requester) {
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
        html.append("        .status-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #007bff; }");
        html.append("        .status-badge { display: inline-block; padding: 5px 15px; background: #17a2b8; color: white; border-radius: 20px; font-weight: bold; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🔄 Exchange Status Updated</h1>");
        html.append("            <p>Your exchange request status has been updated</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(owner.getFirstName()).append("!</h2>");
        html.append("            <p>The status of your exchange request has been updated.</p>");
        html.append("            ");
        html.append("            <div class=\"status-info\">");
        html.append("                <h3>📋 Exchange Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Requested Item:</span> ").append(targetProduct.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Offered Item:</span> ").append(exchangeRequest.getExchangeItemName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Requester:</span> ").append(requester.getFirstName()).append(" ").append(requester.getLastName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">New Status:</span> <span class=\"status-badge\">").append(exchangeRequest.getStatus()).append("</span></div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Updated Date:</span> ").append(exchangeRequest.getCreatedAt()).append("</div>");
        if (exchangeRequest.getRejectionReason() != null && !exchangeRequest.getRejectionReason().isEmpty()) {
            html.append("                <div class=\"info-item\"><span class=\"info-label\">Rejection Reason:</span> ").append(exchangeRequest.getRejectionReason()).append("</div>");
        }
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

    /**
     * Build exchange status update email to requester HTML content
     */
    private String buildExchangeStatusUpdateToRequesterEmail(ExchangeRequest exchangeRequest, Product targetProduct,
                                                             User owner, User requester) {
        String chatLink = baseUrl + "/chat?user=" + owner.getId();

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
        html.append("        .status-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #28a745; }");
        html.append("        .status-badge { display: inline-block; padding: 5px 15px; color: white; border-radius: 20px; font-weight: bold; }");
        html.append("        .approved { background: #28a745; }");
        html.append("        .rejected { background: #dc3545; }");
        html.append("        .pending { background: #ffc107; }");
        html.append("        .button { display: inline-block; padding: 12px 30px; background: #17a2b8; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 10px; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🔄 Exchange Request Update</h1>");
        html.append("            <p>Your exchange request status has been updated</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(requester.getFirstName()).append("!</h2>");
        html.append("            <p>The status of your exchange request has been updated by the item owner.</p>");
        html.append("            ");
        html.append("            <div class=\"status-info\">");
        html.append("                <h3>📋 Exchange Details</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Requested Item:</span> ").append(targetProduct.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Offered Item:</span> ").append(exchangeRequest.getExchangeItemName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Item Owner:</span> ").append(owner.getFirstName()).append(" ").append(owner.getLastName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">New Status:</span> ");
        html.append("                    <span class=\"status-badge ");
        html.append(getStatusClass(exchangeRequest.getStatus()));
        html.append("\">").append(exchangeRequest.getStatus()).append("</span>");
        html.append("                </div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Updated Date:</span> ").append(exchangeRequest.getCreatedAt()).append("</div>");
        if (exchangeRequest.getRejectionReason() != null && !exchangeRequest.getRejectionReason().isEmpty()) {
            html.append("                <div class=\"info-item\"><span class=\"info-label\">Rejection Reason:</span> ").append(exchangeRequest.getRejectionReason()).append("</div>");
        }
        html.append("            </div>");
        html.append("            ");

        // Show chat button if request is approved
        if ("APPROVED".equalsIgnoreCase(exchangeRequest.getStatus())) {
            html.append("            <div style=\"background: #e7f3ff; padding: 20px; border-radius: 10px; margin: 20px 0; text-align: center;\">");
            html.append("                <h3>💬 Start Chatting</h3>");
            html.append("                <p>Your exchange request has been approved! You can now chat with the item owner to arrange the exchange details.</p>");
            html.append("                <a href=\"").append(chatLink).append("\" class=\"button\">Open Chat</a>");
            html.append("            </div>");
        }

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

    /**
     * Helper method to get CSS class for status badge
     */
    private String getStatusClass(String status) {
        if (status == null) return "pending";
        switch (status.toUpperCase()) {
            case "APPROVED":
                return "approved";
            case "REJECTED":
                return "rejected";
            case "PENDING":
            default:
                return "pending";
        }
    }

    // EXCHANGE EMAIL TEMPLATES END HERE

    /**
     * Build password reset email HTML content
     */
    private String buildPasswordResetEmail(String firstName, String resetLink) {
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
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .reset-button { display: inline-block; padding: 12px 30px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🔐 Password Reset</h1>");
        html.append("            <p>Care & Share Account Security</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(firstName).append("!</h2>");
        html.append("            <p>We received a request to reset your password for your Care & Share account.</p>");
        html.append("            <p>Click the button below to create a new password:</p>");
        html.append("            <p style=\"text-align: center;\">");
        html.append("                <a href=\"").append(resetLink).append("\" class=\"reset-button\">Reset Your Password</a>");
        html.append("            </p>");
        html.append("            <p>If you didn't request this reset, please ignore this email. Your password will remain unchanged.</p>");
        html.append("            <p><strong>Note:</strong> This link will expire in 1 hour for security reasons.</p>");
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
        html.append("            <h1>✅ Purchase Confirmed!</h1>");
        html.append("            <p>Thank you for your purchase</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(purchase.getFirstName()).append("!</h2>");
        html.append("            <p>Your purchase has been confirmed. Here are your order details:</p>");
        html.append("            ");
        html.append("            <div class=\"order-details\">");
        html.append("                <h3>📦 Order Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Order ID:</span> #").append(purchase.getId()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Product:</span> ").append(product.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Category:</span> ").append(product.getCategory()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Description:</span> ").append(product.getDescription()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Condition:</span> ").append(product.getCondition()).append("</div>");
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
        html.append("                <h3>ℹ️ Next Steps</h3>");
        html.append("                <p>Please contact the seller to arrange pickup/delivery details.</p>");
        html.append("                <p>You can also use our chat feature to communicate directly with the seller.</p>");
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
        html.append("        .header { background: linear-gradient(135deg, #007bff, #0056b3); padding: 30px; text-align: center; color: white; }");
        html.append("        .content { padding: 30px; background: #f8f9fa; }");
        html.append("        .order-details { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .buyer-info { background: white; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("        .footer { text-align: center; padding: 20px; background: #343a40; color: white; font-size: 12px; }");
        html.append("        .info-item { margin: 10px 0; }");
        html.append("        .info-label { font-weight: bold; color: #007bff; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>🎉 Your Item Sold!</h1>");
        html.append("            <p>Congratulations on your sale</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(seller.getFirstName()).append("!</h2>");
        html.append("            <p>Great news! Your item has been purchased. Here are the sale details:</p>");
        html.append("            ");
        html.append("            <div class=\"order-details\">");
        html.append("                <h3>📦 Sale Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Order ID:</span> #").append(purchase.getId()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Product:</span> ").append(product.getName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Category:</span> ").append(product.getCategory()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Description:</span> ").append(product.getDescription()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Sale Date:</span> ").append(purchase.getCreatedAt()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div class=\"buyer-info\">");
        html.append("                <h3>👤 Buyer Information</h3>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Name:</span> ").append(purchase.getFirstName()).append(" ").append(purchase.getLastName()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Email:</span> ").append(purchase.getEmail()).append("</div>");
        html.append("                <div class=\"info-item\"><span class=\"info-label\">Phone:</span> ").append(purchase.getPhone()).append("</div>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <div style=\"background: #e7f3ff; padding: 20px; border-radius: 10px; margin: 20px 0;\">");
        html.append("                <h3>📞 Next Steps</h3>");
        html.append("                <p>Please contact the buyer within 24 hours to arrange pickup/delivery.</p>");
        html.append("                <p>You can update the order status in your seller dashboard as you progress through the sale process.</p>");
        html.append("            </div>");
        html.append("            ");
        html.append("            <p>Thank you for being part of <strong>Care & Share</strong> community!</p>");
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
}