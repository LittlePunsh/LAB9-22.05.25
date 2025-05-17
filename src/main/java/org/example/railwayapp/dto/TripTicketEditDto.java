package org.example.railwayapp.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// DTO для формы редактирования, объединяющий данные рейса и билета
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripTicketEditDto {
    private Integer tripId;
    private Long ticketId;

    // Поля рейса
    private String trainNumber;
    private String departureStation;
    private String arrivalStation;
    private String departureTime;
    private String arrivalTime;
    private String passengerName;
    private String seatNumber;

    public static TripTicketEditDto fromAdminTripDto(AdminTripDto adminDto) {
        TripTicketEditDto editDto = new TripTicketEditDto();
        editDto.setTripId(adminDto.getTripId());
        editDto.setTicketId(adminDto.getTicketId());
        editDto.setTrainNumber(adminDto.getTrainNumber());
        editDto.setDepartureStation(adminDto.getDepartureStation());
        editDto.setArrivalStation(adminDto.getArrivalStation());
        editDto.setDepartureTime(adminDto.getDepartureTime());
        editDto.setArrivalTime(adminDto.getArrivalTime());
        editDto.setPassengerName(adminDto.getPassengerName());
        editDto.setSeatNumber(adminDto.getSeatNumber());
        return editDto;
    }

    public boolean hasTicket() {
        return this.ticketId != null && this.ticketId > 0;
    }
}