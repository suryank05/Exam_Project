package com.ExamPort.ExamPort.Service;

import com.ExamPort.ExamPort.Entity.Exam;
import com.ExamPort.ExamPort.Entity.Question;
import com.ExamPort.ExamPort.Entity.Result;
import com.ExamPort.ExamPort.Entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Send exam result notification to student (Plain Text)
     */
    public void sendExamResultNotification(User student, Exam exam, Result result) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Skipping email for user: {}", student.getEmail());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(student.getEmail());
            message.setSubject("Exam Result - " + exam.getTitle());
            
            String emailBody = buildPlainTextEmailBody(student, exam, result);
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Plain text exam result email sent successfully to: {}", student.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send plain text exam result email to: {}", student.getEmail(), e);
        }
    }
    
    /**
     * Send exam result notification to student (HTML Format)
     */
    public void sendExamResultNotificationHtml(User student, Exam exam, Result result, Map<String, String> answers) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Skipping HTML email for user: {}", student.getEmail());
            return;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(student.getEmail());
            helper.setSubject("🎓 Exam Result - " + exam.getTitle());
            
            String htmlBody = buildHtmlEmailBody(student, exam, result, answers);
            helper.setText(htmlBody, true);
            
            mailSender.send(mimeMessage);
            logger.info("HTML exam result email sent successfully to: {}", student.getEmail());
            
        } catch (MessagingException e) {
            logger.error("Failed to send HTML exam result email to: {}", student.getEmail(), e);
            // Fallback to plain text email
            sendExamResultNotification(student, exam, result);
        }
    }
    
    /**
     * Build plain text email body
     */
    private String buildPlainTextEmailBody(User student, Exam exam, Result result) {
        double totalMarks = exam.getTotalMarks() > 0 ? exam.getTotalMarks() : 
                           (exam.getQuestions() != null ? exam.getQuestions().stream()
                               .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1).sum() : 0);
        
        double percentage = totalMarks > 0 ? (result.getScore() / totalMarks) * 100 : 0;
        String grade = calculateGrade(percentage);
        
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(student.getFullName() != null ? student.getFullName() : student.getUsername()).append(",\n\n");
        body.append("Thank you for completing the exam: ").append(exam.getTitle()).append("\n\n");
        body.append("Here are your results:\n");
        body.append("================================\n");
        body.append("Score: ").append(String.format("%.1f", result.getScore())).append("/").append(String.format("%.0f", totalMarks)).append("\n");
        body.append("Percentage: ").append(String.format("%.1f", percentage)).append("%\n");
        body.append("Grade: ").append(grade).append("\n");
        body.append("Status: ").append(result.getPassed() ? "PASSED ✓" : "FAILED ✗").append("\n");
        body.append("Time Taken: ").append(formatTime(result.getTimeTaken())).append("\n");
        body.append("Submitted: ").append(result.getAttemptDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))).append("\n");
        body.append("================================\n\n");
        
        if (result.getFeedback() != null && !result.getFeedback().trim().isEmpty()) {
            body.append("Feedback:\n").append(result.getFeedback()).append("\n\n");
        }
        
        body.append("Thank you for using ExamWizards!\n\n");
        body.append("Best regards,\n");
        body.append("ExamWizards Team\n");
        body.append("https://examwizards.com");
        
        return body.toString();
    }
    
    /**
     * Build HTML email body with enhanced formatting
     */
    private String buildHtmlEmailBody(User student, Exam exam, Result result, Map<String, String> answers) {
        double totalMarks = exam.getTotalMarks() > 0 ? exam.getTotalMarks() : 
                           (exam.getQuestions() != null ? exam.getQuestions().stream()
                               .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1).sum() : 0);
        
        double percentage = totalMarks > 0 ? (result.getScore() / totalMarks) * 100 : 0;
        String grade = calculateGrade(percentage);
        String statusColor = result.getPassed() ? "#10B981" : "#EF4444";
        String statusText = result.getPassed() ? "PASSED ✓" : "FAILED ✗";
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Exam Result</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f8fafc; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }");
        html.append(".content { padding: 30px; }");
        html.append(".result-card { background: #f8fafc; border-radius: 8px; padding: 20px; margin: 20px 0; border-left: 4px solid #667eea; }");
        html.append(".score-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 15px; margin: 20px 0; }");
        html.append(".score-item { text-align: center; padding: 15px; background: #f1f5f9; border-radius: 8px; }");
        html.append(".score-value { font-size: 24px; font-weight: bold; color: #1e293b; }");
        html.append(".score-label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }");
        html.append(".status-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: 600; font-size: 14px; }");
        html.append(".feedback { background: #fef3c7; border: 1px solid #f59e0b; border-radius: 8px; padding: 15px; margin: 20px 0; }");
        html.append(".footer { background: #1e293b; color: #94a3b8; text-align: center; padding: 20px; font-size: 14px; }");
        html.append(".footer a { color: #60a5fa; text-decoration: none; }");
        html.append("@media (max-width: 600px) { .score-grid { grid-template-columns: repeat(2, 1fr); } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>🎓 Exam Result</h1>");
        html.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>").append(exam.getTitle()).append("</p>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        html.append("<h2 style='color: #1e293b; margin-bottom: 10px;'>Dear ").append(student.getFullName() != null ? student.getFullName() : student.getUsername()).append(",</h2>");
        html.append("<p style='color: #64748b; margin-bottom: 25px;'>Thank you for completing your exam. Here are your detailed results:</p>");
        
        // Result Card
        html.append("<div class='result-card'>");
        html.append("<div class='score-grid'>");
        
        html.append("<div class='score-item'>");
        html.append("<div class='score-value'>").append(String.format("%.1f", result.getScore())).append("/").append(String.format("%.0f", totalMarks)).append("</div>");
        html.append("<div class='score-label'>Score</div>");
        html.append("</div>");
        
        html.append("<div class='score-item'>");
        html.append("<div class='score-value'>").append(String.format("%.1f", percentage)).append("%</div>");
        html.append("<div class='score-label'>Percentage</div>");
        html.append("</div>");
        
        html.append("<div class='score-item'>");
        html.append("<div class='score-value'>").append(grade).append("</div>");
        html.append("<div class='score-label'>Grade</div>");
        html.append("</div>");
        
        html.append("<div class='score-item'>");
        html.append("<div class='score-value'>").append(formatTime(result.getTimeTaken())).append("</div>");
        html.append("<div class='score-label'>Time Taken</div>");
        html.append("</div>");
        
        html.append("</div>");
        
        // Status Badge
        html.append("<div style='text-align: center; margin-top: 20px;'>");
        html.append("<span class='status-badge' style='background-color: ").append(statusColor).append("; color: white;'>").append(statusText).append("</span>");
        html.append("</div>");
        
        html.append("</div>");
        
        // Submission Details
        html.append("<div style='background: #f8fafc; border-radius: 8px; padding: 15px; margin: 20px 0;'>");
        html.append("<h3 style='margin: 0 0 10px 0; color: #1e293b; font-size: 16px;'>📅 Submission Details</h3>");
        html.append("<p style='margin: 5px 0; color: #64748b;'><strong>Submitted:</strong> ").append(result.getAttemptDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))).append("</p>");
        html.append("<p style='margin: 5px 0; color: #64748b;'><strong>Duration:</strong> ").append(formatTime(result.getTimeTaken())).append("</p>");
        html.append("</div>");
        
        // Feedback
        if (result.getFeedback() != null && !result.getFeedback().trim().isEmpty()) {
            html.append("<div class='feedback'>");
            html.append("<h3 style='margin: 0 0 10px 0; color: #92400e; font-size: 16px;'>💬 Feedback</h3>");
            html.append("<p style='margin: 0; color: #92400e;'>").append(result.getFeedback()).append("</p>");
            html.append("</div>");
        }
        
        // Question-wise feedback (if available)
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty() && answers != null && !answers.isEmpty()) {
            html.append("<div style='margin: 25px 0;'>");
            html.append("<h3 style='color: #1e293b; margin-bottom: 15px;'>📝 Question Summary</h3>");
            
            int correctCount = 0;
            int totalQuestions = exam.getQuestions().size();
            
            for (Question question : exam.getQuestions()) {
                String userAnswer = answers.get(String.valueOf(question.getQue_id()));
                boolean isCorrect = isAnswerCorrect(question, userAnswer);
                if (isCorrect) correctCount++;
                
                String statusIcon = isCorrect ? "✅" : "❌";
                String statusColor2 = isCorrect ? "#10B981" : "#EF4444";
                
                html.append("<div style='border-left: 3px solid ").append(statusColor2).append("; padding: 10px 15px; margin: 10px 0; background: #f8fafc;'>");
                html.append("<p style='margin: 0; font-weight: 600; color: #1e293b;'>").append(statusIcon).append(" Question ").append(question.getQue_id()).append("</p>");
                html.append("<p style='margin: 5px 0 0 0; color: #64748b; font-size: 14px;'>").append(question.getQuestion()).append("</p>");
                html.append("</div>");
            }
            
            html.append("<div style='text-align: center; margin-top: 15px; padding: 15px; background: #e0f2fe; border-radius: 8px;'>");
            html.append("<p style='margin: 0; color: #0277bd; font-weight: 600;'>Questions Answered Correctly: ").append(correctCount).append("/").append(totalQuestions).append("</p>");
            html.append("</div>");
        }
        
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<p style='color: #64748b; font-size: 16px;'>Thank you for using <strong>ExamWizards</strong>!</p>");
        html.append("</div>");
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p style='margin: 0;'>Best regards,<br><strong>ExamWizards Team</strong></p>");
        html.append("<p style='margin: 10px 0 0 0;'><a href='https://examwizards.com'>Visit ExamWizards</a></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Check if the user's answer is correct
     */
    private boolean isAnswerCorrect(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty() || 
            question.getCorrect_options() == null || question.getCorrect_options().isEmpty()) {
            return false;
        }
        
        if ("mcq".equals(question.getType())) {
            // Single choice
            int correctOptionIndex = question.getCorrect_options().get(0);
            if (question.getOptions() != null && correctOptionIndex < question.getOptions().size()) {
                String correctAnswer = question.getOptions().get(correctOptionIndex).getAvailableOption();
                return correctAnswer != null && correctAnswer.equals(userAnswer.trim());
            }
        } else if ("multiple".equals(question.getType())) {
            // Multiple choice - check if all correct options are selected
            String[] userAnswers = userAnswer.split(",");
            java.util.List<String> userAnswersList = java.util.Arrays.stream(userAnswers)
                .map(String::trim)
                .collect(java.util.stream.Collectors.toList());
            
            java.util.List<String> correctAnswers = question.getCorrect_options().stream()
                .filter(index -> question.getOptions() != null && index < question.getOptions().size())
                .map(index -> question.getOptions().get(index).getAvailableOption())
                .collect(java.util.stream.Collectors.toList());
            
            return correctAnswers.size() == userAnswersList.size() &&
                   correctAnswers.containsAll(userAnswersList);
        }
        
        return false;
    }
    
    /**
     * Calculate grade based on percentage
     */
    private String calculateGrade(double percentage) {
        if (percentage >= 95) return "A+";
        if (percentage >= 90) return "A";
        if (percentage >= 85) return "B+";
        if (percentage >= 80) return "B";
        if (percentage >= 75) return "C+";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
    
    /**
     * Format time in seconds to readable format
     */
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
    
    /**
     * Send email verification email
     */
    public void sendVerificationEmail(String toEmail, String token) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Skipping verification email for: {}", toEmail);
            return;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Verify Your Email - ExamWizards");
            
            String verificationUrl = "http://localhost:5173/verify-email?token=" + token;
            String htmlBody = buildVerificationEmailBody(toEmail, verificationUrl);
            helper.setText(htmlBody, true);
            
            mailSender.send(mimeMessage);
            logger.info("Email verification email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send email verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Skipping password reset email for: {}", toEmail);
            return;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔑 Reset Your Password - ExamWizards");
            
            String resetUrl = "http://localhost:5173/reset-password?token=" + token;
            String htmlBody = buildPasswordResetEmailBody(toEmail, resetUrl);
            helper.setText(htmlBody, true);
            
            mailSender.send(mimeMessage);
            logger.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    /**
     * Build email verification email body
     */
    private String buildVerificationEmailBody(String email, String verificationUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Verify Your Email</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f8fafc; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }");
        html.append(".content { padding: 30px; }");
        html.append(".button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: 600; margin: 20px 0; }");
        html.append(".footer { background: #1e293b; color: #94a3b8; text-align: center; padding: 20px; font-size: 14px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>🔐 Verify Your Email</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        html.append("<h2 style='color: #1e293b; margin-bottom: 20px;'>Welcome to ExamWizards!</h2>");
        html.append("<p style='color: #64748b; margin-bottom: 25px;'>Thank you for registering with ExamWizards. To complete your registration and start using our platform, please verify your email address by clicking the button below:</p>");
        
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(verificationUrl).append("' class='button'>Verify Email Address</a>");
        html.append("</div>");
        
        html.append("<p style='color: #64748b; font-size: 14px; margin-top: 30px;'>If the button doesn't work, you can copy and paste this link into your browser:</p>");
        html.append("<p style='color: #667eea; font-size: 14px; word-break: break-all;'>").append(verificationUrl).append("</p>");
        
        html.append("<div style='background: #fef3c7; border: 1px solid #f59e0b; border-radius: 8px; padding: 15px; margin: 20px 0;'>");
        html.append("<p style='color: #92400e; margin: 0; font-size: 14px;'><strong>Important:</strong> This verification link will expire in 24 hours for security reasons.</p>");
        html.append("</div>");
        
        html.append("<p style='color: #64748b; margin-top: 30px;'>If you didn't create an account with ExamWizards, please ignore this email.</p>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p style='margin: 0;'>Best regards,<br><strong>ExamWizards Team</strong></p>");
        html.append("<p style='margin: 10px 0 0 0;'>© 2024 ExamWizards. All rights reserved.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Build password reset email body
     */
    private String buildPasswordResetEmailBody(String email, String resetUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Reset Your Password</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f8fafc; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); color: white; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }");
        html.append(".content { padding: 30px; }");
        html.append(".button { display: inline-block; background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: 600; margin: 20px 0; }");
        html.append(".footer { background: #1e293b; color: #94a3b8; text-align: center; padding: 20px; font-size: 14px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>🔑 Reset Your Password</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        html.append("<h2 style='color: #1e293b; margin-bottom: 20px;'>Password Reset Request</h2>");
        html.append("<p style='color: #64748b; margin-bottom: 25px;'>We received a request to reset your password for your ExamWizards account. Click the button below to create a new password:</p>");
        
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(resetUrl).append("' class='button'>Reset Password</a>");
        html.append("</div>");
        
        html.append("<p style='color: #64748b; font-size: 14px; margin-top: 30px;'>If the button doesn't work, you can copy and paste this link into your browser:</p>");
        html.append("<p style='color: #ef4444; font-size: 14px; word-break: break-all;'>").append(resetUrl).append("</p>");
        
        html.append("<div style='background: #fef2f2; border: 1px solid #ef4444; border-radius: 8px; padding: 15px; margin: 20px 0;'>");
        html.append("<p style='color: #dc2626; margin: 0; font-size: 14px;'><strong>Security Notice:</strong> This password reset link will expire in 30 minutes for your security.</p>");
        html.append("</div>");
        
        html.append("<p style='color: #64748b; margin-top: 30px;'>If you didn't request a password reset, please ignore this email. Your password will remain unchanged.</p>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p style='margin: 0;'>Best regards,<br><strong>ExamWizards Team</strong></p>");
        html.append("<p style='margin: 10px 0 0 0;'>© 2024 ExamWizards. All rights reserved.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * Send test email to verify configuration
     */
    public boolean sendTestEmail(String toEmail) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Cannot send test email.");
            return false;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("ExamWizards - Email Configuration Test");
            message.setText("This is a test email to verify that the email configuration is working correctly.\n\nIf you receive this email, the email service is configured properly!\n\nBest regards,\nExamWizards Team");
            
            mailSender.send(message);
            logger.info("Test email sent successfully to: {}", toEmail);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send test email to: {}", toEmail, e);
            return false;
        }
    }
}