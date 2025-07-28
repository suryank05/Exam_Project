package com.ExamPort.ExamPort.Service;

import com.ExamPort.ExamPort.Entity.ContactMessage;
import com.ExamPort.ExamPort.Repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactMessageService {
    @Autowired
    private ContactMessageRepository repository;

    public ContactMessage save(ContactMessage message) {
        // Additional business logic or notifications could go here
        return repository.save(message);
    }
}
