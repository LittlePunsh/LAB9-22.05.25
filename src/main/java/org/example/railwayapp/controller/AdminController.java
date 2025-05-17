package org.example.railwayapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.example.railwayapp.dto.AdminTripDto;
import org.example.railwayapp.dto.TripTicketEditDto;
import org.example.railwayapp.model.railway.Ticket;
import org.example.railwayapp.model.railway.Trip;
import org.example.railwayapp.model.users.User;
import org.example.railwayapp.service.RailwayDataService;
import org.example.railwayapp.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RailwayDataService railwayDataService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Главная страница админа ---
    @GetMapping
    public String adminPage(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        User loggedInUser = userOptional.orElse(null);
        if (loggedInUser == null) {
            return "redirect:/logout";
        }

        try {
            // Получаем данные о рейсах и билетах для отображения в таблице админа
            List<AdminTripDto> tripsAndTickets = railwayDataService.getAllTripsWithTicketsForAdmin();
            model.addAttribute("tripsData", tripsAndTickets); // Добавляем список в модель

            // Получаем список НЕудаленных пользователей для отображения в таблице
            List<User> nonDeletedUsers = userService.findAllNonDeletedUsers();
            model.addAttribute("usersData", nonDeletedUsers);

            model.addAttribute("loggedInUser", loggedInUser);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка загрузки данных: " + e.getMessage());
        }
        return "admin";
    }

    // --- Методы для добавления Рейса ---

    @GetMapping("/trips/add")
    public String showAddTripTicketForm(Model model) {
        model.addAttribute("formData", new TripTicketEditDto());
        return "admin/add-trip-ticket-form";
    }

    @PostMapping("/trips/add")
    public String processAddTripTicketForm(@ModelAttribute TripTicketEditDto formData, RedirectAttributes redirectAttributes) {

        if (formData.getTripId() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении: ID рейса не должен быть указан.");
            return "redirect:/admin";
        }

        try {
            // 1. Создаем и сохраняем сущность Trip из данных DTO
            Trip newTrip = new Trip();
            newTrip.setTrainNumber(formData.getTrainNumber());
            newTrip.setDepartureStation(formData.getDepartureStation());
            newTrip.setArrivalStation(formData.getArrivalStation());
            newTrip.setDepartureTime(formData.getDepartureTime());
            newTrip.setArrivalTime(formData.getArrivalTime());

            Trip savedTrip = railwayDataService.saveTrip(newTrip);

            // 2. Проверяем, были ли заполнены поля билета в форме. Если да, создаем и сохраняем сущность Ticket.
            if (formData.getPassengerName() != null && !formData.getPassengerName().trim().isEmpty() &&
                    formData.getSeatNumber() != null && !formData.getSeatNumber().trim().isEmpty()) {

                Ticket newTicket = new Ticket();
                newTicket.setTrip(savedTrip);
                newTicket.setPassengerName(formData.getPassengerName().trim());
                newTicket.setSeatNumber(formData.getSeatNumber().trim());
                newTicket.setPurchaseDate(LocalDateTime.now());

                // Сохраняем новый билет
                railwayDataService.saveTicket(newTicket);

                redirectAttributes.addFlashAttribute("successMessage", "Рейс и билет успешно добавлены.");

            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Рейс успешно добавлен (без билета).");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении рейса/билета: " + e.getMessage());

        }
        return "redirect:/admin";
    }
    // Удаляшка
    @PostMapping("/trips/delete/{id}")
    public String deleteTrip(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            railwayDataService.deleteTripById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Рейс успешно удален.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления рейса: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // --- Редактирование Рейса и Билета ---

    @GetMapping("/edit/{tripId}/{ticketId}")
    public String editCombinedForm(@PathVariable("tripId") Integer tripId,
                                   @PathVariable("ticketId") Long ticketId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        // 1. Находим рейс по tripId
        Optional<Trip> tripOptional = railwayDataService.findTripById(tripId);
        if (tripOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Рейс не найден.");
            return "redirect:/admin";
        }

        Trip trip = tripOptional.get();
        Ticket ticket = null;

        // 2. Если ticketId передан и не 0, пытаемся найти билет
        if (ticketId != null && ticketId > 0) {
            Optional<Ticket> ticketOptional = railwayDataService.findTicketById(ticketId);
            if (ticketOptional.isPresent()) {
                ticket = ticketOptional.get();
                if (!ticket.getTrip().getTripId().equals(tripId)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Билет с ID " + ticketId + " не принадлежит рейсу с ID " + tripId + ".");
                    return "redirect:/admin";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Билет с ID " + ticketId + " не найден.");
                return "redirect:/admin";
            }
        }

        // 3. Создаем DTO для формы редактирования
        TripTicketEditDto editData = new TripTicketEditDto();
        editData.setTripId(trip.getTripId());
        editData.setTrainNumber(trip.getTrainNumber());
        editData.setDepartureStation(trip.getDepartureStation());
        editData.setArrivalStation(trip.getArrivalStation());
        editData.setDepartureTime(trip.getDepartureTime());
        editData.setArrivalTime(trip.getArrivalTime());

        if (ticket != null) {

            editData.setTicketId(ticket.getTicketId());
            editData.setPassengerName(ticket.getPassengerName());
            editData.setSeatNumber(ticket.getSeatNumber());
        } else {

            editData.setTicketId(null);
            editData.setPassengerName("---");
            editData.setSeatNumber("---");
        }

        // 4. Передаем заполненный DTO в модель для связывания с формой
        model.addAttribute("editData", editData);

        // 5. Возвращаем имя шаблона формы редактирования
        return "admin/trip-ticket-edit-form";
    }

    @PostMapping("/update-combined")
    public String processCombinedUpdate(@ModelAttribute TripTicketEditDto editData, RedirectAttributes redirectAttributes) {

        if (editData.getTripId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректные данные для обновления: отсутствует ID рейса.");
            return "redirect:/admin";
        }

        try {
            // 1. Находим существующую сущность Trip и обновляем ее поля
            Optional<Trip> tripOptional = railwayDataService.findTripById(editData.getTripId());
            if (tripOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Рейс для обновления не найден (ID: " + editData.getTripId() + ").");
                return "redirect:/admin";
            }
            Trip tripToUpdate = tripOptional.get();
            tripToUpdate.setTrainNumber(editData.getTrainNumber());
            tripToUpdate.setDepartureStation(editData.getDepartureStation());
            tripToUpdate.setArrivalStation(editData.getArrivalStation());
            tripToUpdate.setDepartureTime(editData.getDepartureTime());
            tripToUpdate.setArrivalTime(editData.getArrivalTime());

            // Сохраняем обновленный рейс
            railwayDataService.saveTrip(tripToUpdate);

            // 2. Если в DTO присутствует ID билета
            if (editData.hasTicket()) {
                Optional<Ticket> ticketOptional = railwayDataService.findTicketById(editData.getTicketId());
                if (ticketOptional.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Билет для обновления не найден (ID: " + editData.getTicketId() + ").");
                    return "redirect:/admin";
                }
                Ticket ticketToUpdate = ticketOptional.get();

                // Обновляем поля билета
                ticketToUpdate.setPassengerName(editData.getPassengerName());
                ticketToUpdate.setSeatNumber(editData.getSeatNumber());
                // purchaseDate и связь с Trip НЕ меняются через эту форму

                // Сохраняем обновленный билет
                railwayDataService.saveTicket(ticketToUpdate);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Данные успешно обновлены.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка обновления данных: " + e.getMessage());

        }

        return "redirect:/admin";
    }

    // Удаление
    @PostMapping("/tickets/delete/{id}")
    public String deleteTicket(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // Вызываем сервис для удаления билета по ID
            railwayDataService.deleteTicketById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Билет успешно удален.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления билета: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // --- Методы для CRUD Пользователей ---

    // Показать форму добавления нового пользователя (обрабатывает GET запрос на /admin/users/add)
    @GetMapping("/users/add")
    public String addUserForm(Model model) {
        // Передаем пустой объект User для связывания с формой
        model.addAttribute("user", new User());
        // Сообщения об ошибке/успехе могут быть переданы flash attributes
        return "admin/user-form"; // Возвращаем имя шаблона формы пользователя
    }

    // Обработать сохранение нового пользователя (админом) (обрабатывает POST запрос на /admin/users)
    // Принимает User объект для полей username, email, role И @RequestParam String password для сырого пароля из формы.
    @PostMapping("/users")
    public String saveUser(@ModelAttribute User user, // Связывает поля username, email, role
                           @RequestParam String password, // Получает сырой пароль из поля name="password"
                           RedirectAttributes redirectAttributes) {

        // При добавлении ID пользователя должен быть null. Если он почему-то есть, это некорректный запрос.
        if (user.getId() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректные данные для создания пользователя (присутствует ID).");
            // Возвращаемся на главную админку или форму добавления с ошибкой
            return "redirect:/admin";
        }
        // Проверяем, что пароль не пустой при добавлении нового пользователя
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароль обязателен для нового пользователя.");
            // Возвращаемся на форму добавления с ошибкой
            return "redirect:/admin/users/add";
        }

        try {

            userService.registerUser(user.getUsername(), password, user.getEmail()); // registerUser ставит роль "user"

            redirectAttributes.addFlashAttribute("successMessage", "Пользователь " + user.getUsername() + " успешно добавлен.");

        } catch (Exception e) {
            // В случае ошибки (например, уникальность), добавляем сообщение об ошибке
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка добавления пользователя: " + e.getMessage());
            // Возвращаемся на форму добавления с ошибкой (и, возможно, с введенными данными)
            // redirectAttributes.addFlashAttribute("user", user); // Сохраняем введенные данные
            return "redirect:/admin/users/add";
        }
        // Перенаправляем обратно на главную страницу админа после успешного добавления
        return "redirect:/admin";
    }


    // Показать форму редактирования пользователя (для админа) (обрабатывает GET запрос на /admin/users/edit/{id})
    // Админ может редактировать любого пользователя по ID, включая удаленных.
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        // Используем findUserById, который ищет пользователя независимо от флага deleted
        Optional<User> userOptional = userService.findUserById(id);

        // Если пользователь не найден, перенаправляем с ошибкой
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден.");
            return "redirect:/admin";
        }

        // Передаем объект User в модель для связывания с формой
        model.addAttribute("user", userOptional.get());
        // ВАЖНО: Пароль НЕ передается в модель и НЕ отображается в форме редактирования (см. user-form.html)
        return "admin/user-form"; // Возвращаем имя шаблона формы пользователя
    }

    // Обработать сохранение обновленного пользователя (админом) (обрабатывает POST запрос на /admin/users/update)
    // Принимает User объект из формы. Пароль и флаг deleted через эту форму не меняются.
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user, // Связывает поля username, email, role из формы
                             RedirectAttributes redirectAttributes) {

        // При обновлении ID пользователя ОБЯЗАТЕЛЕН
        if (user.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректные данные для обновления пользователя (отсутствует ID).");
            return "redirect:/admin";
        }

        try {
            // Находим существующего пользователя в базе, чтобы получить его текущий пароль и флаг deleted
            Optional<User> existingUserOptional = userService.findUserById(user.getId());
            // Если существующий пользователь не найден (удален кем-то другим?), перенаправляем с ошибкой
            if (existingUserOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Пользователь для обновления не найден (ID: " + user.getId() + ").");
                // Возможно, стоит вернуться на форму редактирования этого же DTO с ошибкой
                return "redirect:/admin";
            }
            User existingUser = existingUserOptional.get(); // Получаем сущность

            // Обновляем ТОЛЬКО те поля, которые разрешено менять через эту форму (username, email, role)
            existingUser.setUsername(user.getUsername()); // Можно добавить проверку уникальности при смене username/email
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole()); // Админ может менять роль



            // Сохраняем обновленные данные пользователя
            userService.saveUser(existingUser); // save метод JPA обновит существующую запись по ID

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage", "Данные пользователя " + existingUser.getUsername() + " успешно обновлены.");

        } catch (Exception e) {
            // В случае ошибки обновления, добавляем сообщение об ошибке
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка обновления пользователя: " + e.getMessage());

        }
        // Перенаправляем обратно на главную страницу админа после успешного обновления
        return "redirect:/admin";
    }


    // Обработать мягкое удаление пользователя (обрабатывает POST запрос на /admin/users/delete/{id})
    @PostMapping("/users/delete/{id}")
    public String softDeleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // Получаем информацию о текущем залогиненном пользователе для проверки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> currentUserOptional = userService.findByUsername(currentUsername); // Ищем текущего пользователя (только не удаленного)

        // Проверяем, пытается ли админ удалить самого себя.
        // Проверяем, что текущий пользователь существует и его ID совпадает с ID пользователя, которого пытаются удалить.
        if(currentUserOptional.isPresent() && currentUserOptional.get().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Вы не можете удалить самого себя.");
            return "redirect:/admin"; // Перенаправляем обратно с ошибкой
        }

        try {
            // Вызываем сервис для мягкого удаления пользователя
            userService.softDeleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно отмечен как удаленный.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления пользователя: " + e.getMessage());
        }
        // Перенаправляем обратно на главную страницу админа
        return "redirect:/admin";
    }

    // Обработать восстановление пользователя (обрабатывает POST запрос на /admin/users/restore/{id})
    // Этот метод вызывается из формы на user-form.html, если пользователь был удален
    @PostMapping("/users/restore/{id}")
    public String restoreUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // Вызываем сервис для восстановления пользователя
            userService.restoreUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно восстановлен.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка восстановления пользователя: " + e.getMessage());
        }
        // Перенаправляем обратно на главную страницу админа
        return "redirect:/admin";
    }

    // --- Конец Методов для CRUD Пользователей ---
}