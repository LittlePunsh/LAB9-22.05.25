package org.example.railwayapp.controller;

import org.springframework.security.core.Authentication; // Импорт для получения информации об аутентификации
import org.springframework.security.core.GrantedAuthority; // Импорт для работы с ролями
import org.springframework.security.core.context.SecurityContextHolder; // Импорт для доступа к контексту безопасности
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection; // Импорт для Collection

@Controller // Отмечаем как стандартный MVC контроллер
public class DefaultController {

    // Этот метод обрабатывает GET запрос на /default
    // Он используется как defaultSuccessUrl в SecurityConfig после успешного логина
    @GetMapping("/default")
    public String defaultAfterLogin() {
        // Spring Security гарантирует, что сюда попадет только АУТЕНТИФИЦИРОВАННЫЙ пользователь.
        // Проверка authentication == null или "anonymousUser" здесь скорее для защиты,
        // но при стандартной настройке formLogin сюда неаутентифицированные не попадают.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Получаем набор ролей пользователя из объекта Authentication
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Проверяем наличие ролей "ROLE_ADMIN" или "ROLE_USER"
        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));

        // Перенаправляем на соответствующую страницу в зависимости от роли
        if (isAdmin) {
            return "redirect:/admin"; // Перенаправить на эндпоинт /admin
        } else if (isUser) {
            return "redirect:/user"; // Перенаправить на эндпоинт /user
        } else {
            // Если роль не определена или неизвестна (не должна происходить при правильной настройке ролей)
            // В качестве меры предосторожности, можно очистить контекст безопасности и перенаправить на логин
            SecurityContextHolder.clearContext();
            return "redirect:/login?error=role"; // Перенаправить на логин с индикатором ошибки
        }
    }

    // Этот метод обрабатывает GET запрос на корневой URL "/"
    // SecurityConfig настроен так, что если пользователь не аутентифицирован,
    // он перенаправляется на страницу логина (/loginPage).
    // Если пользователь аутентифицирован, SecurityConfig разрешает доступ к "/",
    // и тогда этот метод перенаправляет его на /default для определения страницы по роли.
    @GetMapping("/")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Проверяем, аутентифицирован ли пользователь и не является ли он анонимным (т.е. залогинен)
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Если залогинен, перенаправляем на /default для определения страницы по роли
            return "redirect:/default";
        }
        // Если не залогинен, этот return технически избыточен при стандартной настройке Spring Security,
        // т.к. SecurityConfig уже перенаправит на /login. Но явно указать не вредно.
        return "redirect:/login"; // Перенаправляем на страницу логина
    }
}