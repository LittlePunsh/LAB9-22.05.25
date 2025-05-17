package org.example.railwayapp.dto;

import org.example.railwayapp.model.railway.Ticket;
import org.example.railwayapp.model.railway.Trip;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTripDto {
    private Integer tripId;
    private String trainNumber;
    private String departureStation;
    private String arrivalStation;
    private String departureTime;
    private String arrivalTime;
    private String passengerName;
    private String seatNumber;

    // === ДОБАВЛЕНО ===
    private Long ticketId; // Добавляем ID билета
    // =================


    public AdminTripDto(Trip trip, Ticket ticket) {
        this.tripId = trip.getTripId();
        this.trainNumber = trip.getTrainNumber();
        this.departureStation = trip.getDepartureStation();
        this.arrivalStation = trip.getArrivalStation();
        this.departureTime = trip.getDepartureTime();
        this.arrivalTime = trip.getArrivalTime();

        if (ticket != null) {
            this.passengerName = ticket.getPassengerName();
            this.seatNumber = ticket.getSeatNumber();
            // === ДОБАВЛЕНО ===
            this.ticketId = ticket.getTicketId(); // Заполняем ID билета, если билет есть
            // =================
        } else {
            this.passengerName = "---";
            this.seatNumber = "---";
            // === ДОБАВЛЕНО ===
            this.ticketId = null; // Нет билета, нет ID
            // =================
        }
    }
}