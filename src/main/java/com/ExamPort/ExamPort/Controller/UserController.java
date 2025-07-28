package com.ExamPort.ExamPort.Controller;

import com.ExamPort.ExamPort.Entity.User;
import com.ExamPort.ExamPort.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            logger.warn("Unauthorized access attempt to /me endpoint");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = principal.getName();
        logger.info("Fetching profile for user: {}", username);
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("User profile not found: {}", username);
                return ResponseEntity.status(404).body("User not found");
            }
            
            User user = userOpt.get();
            logger.debug("Profile retrieved successfully for user: {}", username);
            
            return ResponseEntity.ok(new UserProfileDTO(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.getFullName(),
        user.getAvatarUrl(),
        user.getGender(),
        user.getPhoneNumber()
    ));
        } catch (Exception e) {
            logger.error("Error fetching profile for user: {}", username, e);
            return ResponseEntity.internalServerError().body("Error fetching user profile");
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(java.security.Principal principal, @RequestBody UserProfileDTO update) {
        if (principal == null) {
            logger.warn("Unauthorized profile update attempt");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = principal.getName();
        logger.info("Profile update request for user: {}", username);
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("User not found for profile update: {}", username);
                return ResponseEntity.status(404).body("User not found");
            }
            
            User user = userOpt.get();
            boolean passwordChanged = false;
            
            // Only update allowed fields
            if (update.email != null && !update.email.isBlank()) {
                logger.debug("Updating email for user: {}", username);
                user.setEmail(update.email);
            }
            if (update.fullName != null) user.setFullName(update.fullName);
            if (update.gender != null) user.setGender(update.gender);
            if (update.avatarUrl != null) user.setAvatarUrl(update.avatarUrl);
            if (update.phoneNumber != null && !update.phoneNumber.isBlank()) user.setPhoneNumber(update.phoneNumber);
            
            // Password update logic
            if (update.currentPassword != null && update.newPassword != null && !update.newPassword.isBlank()) {
                logger.info("Password change request for user: {}", username);
                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                if (!encoder.matches(update.currentPassword, user.getPassword())) {
                    logger.warn("Invalid current password provided for user: {}", username);
                    return ResponseEntity.status(400).body("Current password is incorrect");
                }
                user.setPassword(encoder.encode(update.newPassword));
                passwordChanged = true;
                logger.info("Password updated successfully for user: {}", username);
            }
            
            userRepository.save(user);
            logger.info("Profile updated successfully for user: {} (password changed: {})", username, passwordChanged);
            
            return ResponseEntity.ok(new UserProfileDTO(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.getFullName(),
        user.getAvatarUrl(),
        user.getGender(),
        user.getPhoneNumber()
    ));
        } catch (Exception e) {
            logger.error("Error updating profile for user: {}", username, e);
            return ResponseEntity.internalServerError().body("Error updating user profile");
        }
    }

    public static class UserProfileDTO {
        public Long id;
        public String username;
        public String email;
        public String role;
        public String fullName;
        public String avatarUrl;
        public String gender;
        public String phoneNumber;
        public String currentPassword;
        public String newPassword;
        public UserProfileDTO(Long id, String username, String email, String role, String fullName, String avatarUrl, String gender, String phoneNumber) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
            this.gender = gender;
            this.phoneNumber = phoneNumber;
        }
    }
}
