package com.example.website.controller;

import com.example.website.domain.Message;
import com.example.website.domain.User;
import com.example.website.repos.MessageRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MainController {

    private MessageRepo messageRepo;

    @Value("${upload.path}")
    private String uploadPath;

    public MainController(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @GetMapping("/")
    public String greeting(Model model) {
        if(getPrincipal() != null) {
            model.addAttribute("user", getPrincipal());
            return "greeting";
        }

        return "login";
    }

    private User getPrincipal() {
        User user = null;
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User) {
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        return user;
    }

    @GetMapping("/main")
    public String home(
            @RequestParam(required = false, defaultValue = "") String filter,
            Model model,
            @PageableDefault(sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Message> page;

        if (filter != null && !filter.isEmpty()) {
            page = messageRepo.findByTag(filter, pageable);
        } else {
            page = messageRepo.findAll(pageable);
        }

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        model.addAttribute("pageItems", Arrays.asList(5, 10, 25, 50));
        model.addAttribute("user", getPrincipal());
        return "main";
    }


    @PostMapping("/main")
    public String add(
            @AuthenticationPrincipal User user,
            @Valid Message message,
            BindingResult bindingResult,
            Model model,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        message.setAuthor(user);

        if (bindingResult.hasErrors()) {
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);

            model.mergeAttributes(errorsMap);
            model.addAttribute("message", message);
        } else {
            uploadFile(message, file);

            model.addAttribute("message", null);

            messageRepo.save(message);
        }


        Iterable<Message> messages = messageRepo.findAll();

        model.addAttribute("messages", messages);

        return "main";
    }

    private void uploadFile(Message message, MultipartFile file) throws IOException {
        if(file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);

            if(!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File("C:" + uploadPath + "/" + resultFileName));

            message.setFilename(resultFileName);
        }
    }

    @GetMapping("/user-messages/{user}")
    public String userMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User user,
            Model model,
            @RequestParam(required = false) Message message
    ) {
        Set<Message> messages = user.getMessage();

        model.addAttribute("userChannel", user);

        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());

        model.addAttribute("isSubscriber", user.getSubscribers().contains(currentUser));


        model.addAttribute("messages", messages);
        model.addAttribute("message", message);
        model.addAttribute("user", user);
        model.addAttribute("isCurrentUser", currentUser.equals(user));

        return "userMessages";
    }


    @PostMapping("/user-messages/{user}")
    public String updateMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long user,
            @RequestParam("id") Message message,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (message.getAuthor().equals(currentUser)) {
            if (!StringUtils.isEmpty(text)) {
                message.setText(text);
            }

            if (!StringUtils.isEmpty(tag)) {
                message.setTag(tag);
            }

            uploadFile(message, file);

            messageRepo.save(message);
        }

        return "redirect:/user-messages/" + user;
    }
}