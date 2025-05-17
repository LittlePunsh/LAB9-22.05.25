package org.example.railwayapp.service;

import org.example.railwayapp.dto.AdminTripDto;
import org.example.railwayapp.model.railway.Ticket;
import org.example.railwayapp.model.railway.Trip;
import org.example.railwayapp.repository.railway.TicketRepository; // Импорт TicketRepository
import org.example.railwayapp.repository.railway.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RailwayDataService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TicketRepository ticketRepository; // Внедряем TicketRepository

    @Transactional(value = "railwayTransactionManager", readOnly = true)
    public List<Trip> getAllTripsOrdered() {
        return tripRepository.findAllByOrderByDepartureTimeAsc();
    }

    @Transactional(value = "railwayTransactionManager", readOnly = true)
    public List<AdminTripDto> getAllTripsWithTicketsForAdmin() {
        List<Trip> tripsWithTickets = tripRepository.findAllWithTicketsOrderByDepartureTimeAsc();
        return tripsWithTickets.stream()
                .flatMap(trip -> {
                    if (trip.getTickets() == null || trip.getTickets().isEmpty()) {
                        return List.of(new AdminTripDto(trip, null)).stream();
                    }
                    return trip.getTickets().stream()
                            .map(ticket -> new AdminTripDto(trip, ticket));
                })
                .collect(Collectors.toList());
    }

    // --- Методы для CRUD рейсов ---
    @Transactional(value = "railwayTransactionManager", readOnly = true)
    public Optional<Trip> findTripById(Integer tripId) {
        return tripRepository.findById(tripId);
    }

    @Transactional("railwayTransactionManager")
    public Trip saveTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    @Transactional("railwayTransactionManager")
    public void deleteTripById(Integer tripId) {
        tripRepository.deleteById(tripId);
    }

    // === ДОБАВЛЕНО: Методы для CRUD билетов ===

    // Найти билет по ID
    @Transactional(value = "railwayTransactionManager", readOnly = true)
    public Optional<Ticket> findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    // Сохранить или обновить билет
    @Transactional("railwayTransactionManager")
    public Ticket saveTicket(Ticket ticket) {
        // При обновлении билета важно убедиться, что связь с рейсом (Trip) сохраняется.
        // Если в форме редактирования билета нет поля для выбора рейса,
        // нужно найти существующий билет перед сохранением, чтобы не потерять Trip.
        // Это делается в контроллере (AdminController.updateTicket).
        return ticketRepository.save(ticket);
    }

    // Удалить билет по ID (физическое удаление)
    @Transactional("railwayTransactionManager")
    public void deleteTicketById(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }

    // === Конец ДОБАВЛЕННЫХ методов для билетов ===

}