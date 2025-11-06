package com.example.userapp.controller;

import com.example.userapp.entity.User;
import com.example.userapp.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Web controller for User UI operations.
 */
@Controller
public class UserWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserWebController.class);

    private final UserService userService;

    /**
     * Constructor with dependency injection.
     *
     * @param userService the user service
     */
    @Autowired
    public UserWebController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Show the user registration form.
     *
     * @param model the model
     * @return the registration view
     */
    @GetMapping("/")
    public String showRegistrationForm(final Model model) {
        model.addAttribute("user", new User());
        final List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        return "index";
    }

    /**
     * Handle user registration form submission.
     *
     * @param user               the user to register
     * @param bindingResult      validation results
     * @param redirectAttributes redirect attributes for flash messages
     * @return redirect to home page
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute final User user,
                               final BindingResult bindingResult,
                               final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            LOGGER.warn("User registration failed due to validation errors");
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors");
            return "redirect:/";
        }

        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "User registered successfully!");
            LOGGER.info("User registered successfully via web form: {}", user.getEmail());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            LOGGER.warn("User registration failed: {}", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Registration failed. Please try again.");
            LOGGER.error("Unexpected error during user registration", e);
        }

        return "redirect:/";
    }

    /**
     * Health check endpoint.
     *
     * @param model the model
     * @return health check view
     */
    @GetMapping("/health")
    public String healthCheck(final Model model) {
        model.addAttribute("status", "UP");
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "health";
    }
}
