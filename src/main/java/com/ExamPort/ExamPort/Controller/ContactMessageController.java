package com.ExamPort.ExamPort.Controller;

import com.ExamPort.ExamPort.Entity.ContactMessage;
import com.ExamPort.ExamPort.Repository.ContactMessageRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ContactMessageController{
    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @PostMapping
    public ResponseEntity<?> createContactMessage(@Valid @RequestBody ContactMessage contactMessage){
        ContactMessage saved=contactMessageRepository.save(contactMessage);
        return ResponseEntity.ok(saved);
    }
}
