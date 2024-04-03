package com.example.website.service;

import com.example.website.domain.User;
import com.example.website.domain.dto.MessageDTO;
import com.example.website.repos.MessageRepo;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepo messageRepo;

    public Page<MessageDTO> messageList(Pageable pageable, String filter, User user) {
        if (filter != null && !filter.isEmpty()) {
            return messageRepo.findByTag(filter, pageable, user);
        } else {
            return messageRepo.findAll(pageable, user);
        }
    }

    public Page<MessageDTO> messageListForUser(Pageable pageable, User currentUser, User author) {
        return messageRepo.findByUser(pageable, author, currentUser);
    }
}
