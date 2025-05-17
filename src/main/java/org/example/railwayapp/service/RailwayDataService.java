package org.example.railwayapp.service;

import org.example.railwayapp.dto.AdminTripDto;
import org.example.railwayapp.model.railway.Ticket;
import org.example.railwayapp.model.railway.Trip;
import org.example.railwayapp.repository.railway.TicketRepository;
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
    private TicketRepository ticketRepository;

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

    // --- Методы длярейсов ---
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

    // Найти билет по ID
    @Transactional(value = "railwayTransactionManager", readOnly = true)
    public Optional<Ticket> findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    // Сохранить или обновить билет
    @Transactional("railwayTransactionManager")
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    // Удалить билет по ID
    @Transactional("railwayTransactionManager")
    public void deleteTicketById(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }

}