package org.example.railwayapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class DefaultController {

    @GetMapping("/default")
    public String defaultAfterLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Проверяем наличие ролей "ROLE_ADMIN" или "ROLE_USER"
        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
        if (isAdmin) {
            return "redirect:/admin";
        } else if (isUser) {
            return "redirect:/user";
        } else {
            SecurityContextHolder.clearContext();
            return "redirect:/login?error=role";
        }
    }

    @GetMapping("/")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/default";
        }
        return "redirect:/login";
    }
}