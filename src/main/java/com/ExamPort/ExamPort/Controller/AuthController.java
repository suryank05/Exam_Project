package com.ExamPort.ExamPort.Controller;

import com.ExamPort.ExamPort.Entity.User;
import com.ExamPort.ExamPort.Repository.UserRepository;
import com.ExamPort.ExamPort.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        logger.info("Registration attempt for username: {}", user.getUsername());
        
        try {
            if (userRepository.existsByUsername(user.getUsername())) {
                logger.warn("Registration failed - Username already exists: {}", user.getUsername());
                Map<String, String> error = new HashMap<>();
error.put("error", "Username already exists");
return ResponseEntity.badRequest().body(error);
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                logger.warn("Registration failed - Email already exists: {}", user.getEmail());
                Map<String, String> error = new HashMap<>();
error.put("error", "Email already exists");
return ResponseEntity.badRequest().body(error);
            }
            
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(user.getRole() != null ? user.getRole() : "ROLE_STUDENT");
            user.setEmailVerified(false); // Set email as not verified initially
            User savedUser = userRepository.save(user);
            
            // Send email verification
            boolean emailSent = authService.sendEmailVerification(savedUser);
            
            logger.info("User registered successfully: {} with role: {}", user.getUsername(), user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("fullName", user.getFullName());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("gender", user.getGender());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("emailVerified", user.isEmailVerified());
            response.put("verificationEmailSent", emailSent);
            response.put("message", emailSent ? 
                "Registration successful! Please check your email to verify your account." :
                "Registration successful! However, verification email could not be sent.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during user registration for username: {}", user.getUsername(), e);
            Map<String, String> error = new HashMap<>();
error.put("error", "Registration failed due to server error");
return ResponseEntity.internalServerError().body(error);
        }
    }

    @Autowired
    private com.ExamPort.ExamPort.Security.JwtUtil jwtUtil;

    // Health check endpoint for testing
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        logger.info("Health check endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Auth service is running");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        logger.info("=== LOGIN REQUEST RECEIVED ===");
        logger.info("Username: {}", loginRequest.getUsername());
        logger.info("Request URI: /api/auth/login");
        logger.info("Request Method: POST");
        
        try {
            // Check if user exists
            logger.info("Checking if user exists in database...");
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                logger.warn("Login failed - User not found: {}", loginRequest.getUsername());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid username or password");
                errorResponse.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            User user = userOpt.get();
            logger.info("User found: {} with role: {}", user.getUsername(), user.getRole());
            
            // Check password
            logger.info("Verifying password...");
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("Login failed - Invalid password for username: {}", loginRequest.getUsername());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid username or password");
                errorResponse.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            // Generate JWT token
            logger.info("Generating JWT token...");
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            logger.info("User logged in successfully: {} with role: {}", user.getUsername(), user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("fullName", user.getFullName());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("gender", user.getGender());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("=== LOGIN SUCCESSFUL ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("=== LOGIN ERROR ===");
            logger.error("Error during login for username: {}", loginRequest.getUsername(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed due to server error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Email verification endpoint
     * GET /api/auth/verify-email?token=<token>
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        logger.info("Email verification attempt with token: {}", token);
        
        try {
            boolean verified = authService.verifyEmail(token);
            
            Map<String, Object> response = new HashMap<>();
            if (verified) {
                response.put("success", true);
                response.put("message", "Email verified successfully! You can now log in to your account.");
                logger.info("Email verification successful for token: {}", token);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Email verification failed. The token may be invalid or expired.");
                logger.warn("Email verification failed for token: {}", token);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error during email verification for token: {}", token, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Email verification failed due to server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Request password reset endpoint
     * POST /api/auth/request-password-reset
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Password reset requested for email: {}", email);
        
        try {
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean sent = authService.sendPasswordResetToken(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "If an account with this email exists, a password reset link has been sent.");
            
            logger.info("Password reset request processed for email: {}", email);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during password reset request for email: {}", email, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Password reset request failed due to server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Reset password endpoint
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        logger.info("Password reset attempt with token: {}", token);
        
        try {
            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "New password is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (newPassword.length() < 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean reset = authService.resetPassword(token, newPassword);
            
            Map<String, Object> response = new HashMap<>();
            if (reset) {
                response.put("success", true);
                response.put("message", "Password reset successfully! You can now log in with your new password.");
                logger.info("Password reset successful for token: {}", token);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Password reset failed. The token may be invalid or expired.");
                logger.warn("Password reset failed for token: {}", token);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error during password reset for token: {}", token, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Password reset failed due to server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Resend email verification endpoint
     * POST /api/auth/resend-verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Resend verification requested for email: {}", email);
        
        try {
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "No account found with this email address");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            User user = userOpt.get();
            if (user.isEmailVerified()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Email is already verified");
                return ResponseEntity.ok(response);
            }
            
            boolean sent = authService.sendEmailVerification(user);
            
            Map<String, Object> response = new HashMap<>();
            if (sent) {
                response.put("success", true);
                response.put("message", "Verification email sent successfully! Please check your inbox.");
            } else {
                response.put("success", false);
                response.put("message", "Failed to send verification email. Please try again later.");
            }
            
            logger.info("Resend verification processed for email: {}", email);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during resend verification for email: {}", email, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Resend verification failed due to server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
