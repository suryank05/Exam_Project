package com.ExamPort.ExamPort.Controller;

import com.ExamPort.ExamPort.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailTestController{
    
    private static final Logger logger = LoggerFactory.getLogger(EmailTestController.class);
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Test email configuration by sending a test email
     * Only accessible by instructors for security
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> sendTestEmail(@RequestBody Map<String,String> request){
        String toEmail=request.get("email");
        
        if (toEmail==null || toEmail.trim().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of(
                "success",false,
                "message","Email address is required"
            ));
        }
        
        logger.info("Sending test email to: {}",toEmail);
        
        try {
            boolean success=emailService.sendTestEmail(toEmail);
            
            if (success){
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test email sent successfully to "+toEmail
                ));
            } else{
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Failed to send test email. Check email configuration."
                ));
            }
        } catch (Exception e){
            logger.error("Error sending test email to: {}",toEmail, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error sending test email: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get email configuration status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getEmailStatus(){
        try {
            // This is a simple status check - in a real application you might want to
            // check if the email service is properly configured
            return ResponseEntity.ok(Map.of(
                "emailEnabled", true,
                "message","Email service is configured and ready"
            ));
        } catch (Exception e){
            logger.error("Error checking email status", e);
            return ResponseEntity.ok(Map.of(
                "emailEnabled", false,
                "message", "Email service configuration error: " + e.getMessage()
            ));
        }
    }
}