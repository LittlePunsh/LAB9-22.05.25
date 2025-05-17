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

        model.addAttribute("editData", editData);

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

    // GET запрос на /admin/users/add
    @GetMapping("/users/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }


    @PostMapping("/users")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {

        if (user.getId() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректные данные для создания пользователя (присутствует ID).");
            return "redirect:/admin";
        }
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароль обязателен для нового пользователя.");
            return "redirect:/admin/users/add";
        }

        try {

            userService.registerUser(user.getUsername(), password, user.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", "Пользователь " + user.getUsername() + " успешно добавлен.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка добавления пользователя: " + e.getMessage());
            return "redirect:/admin/users/add";
        }
        return "redirect:/admin";
    }


    //GET запрос на /admin/users/edit/{id})
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findUserById(id);

        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден.");
            return "redirect:/admin";
        }

        model.addAttribute("user", userOptional.get());
        return "admin/user-form";
    }

    //POST запрос на /admin/users/update
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user,
                             RedirectAttributes redirectAttributes) {

        if (user.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректные данные для обновления пользователя (отсутствует ID).");
            return "redirect:/admin";
        }

        try {
            Optional<User> existingUserOptional = userService.findUserById(user.getId());
            if (existingUserOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Пользователь для обновления не найден (ID: " + user.getId() + ").");
                return "redirect:/admin";
            }
            User existingUser = existingUserOptional.get();
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());

            userService.saveUser(existingUser);

            redirectAttributes.addFlashAttribute("successMessage", "Данные пользователя " + existingUser.getUsername() + " успешно обновлены.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка обновления пользователя: " + e.getMessage());

        }
        return "redirect:/admin";
    }


    //POST запрос на /admin/users/delete/{id})
    @PostMapping("/users/delete/{id}")
    public String softDeleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> currentUserOptional = userService.findByUsername(currentUsername);

        if(currentUserOptional.isPresent() && currentUserOptional.get().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Вы не можете удалить самого себя.");
            return "redirect:/admin";
        }

        try {
            userService.softDeleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно отмечен как удаленный.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления пользователя: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // POST запрос на /admin/users/restore/{id}
    @PostMapping("/users/restore/{id}")
    public String restoreUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.restoreUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно восстановлен.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка восстановления пользователя: " + e.getMessage());
        }
        return "redirect:/admin";
    }

}