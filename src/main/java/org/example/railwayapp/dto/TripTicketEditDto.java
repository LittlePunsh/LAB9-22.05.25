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
    private Integer tripId; // ID рейса (скрытое поле в форме)
    private Long ticketId;  // ID билета (скрытое поле в форме, может быть null/0)

    // Поля рейса (из сущности Trip)
    private String trainNumber;
    private String departureStation;
    private String arrivalStation;
    private String departureTime; // Предполагается формат HH:mm
    private String arrivalTime;   // Предполагается формат HH:mm

    // Поля билета (из сущности Ticket), могут быть null, если билета нет
    private String passengerName;
    private String seatNumber;

    // Можно добавить purchaseDate, если нужно отобразить, но не редактировать
    // private LocalDateTime purchaseDate;

    // Метод для удобства создания DTO из AdminTripDto
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
        // Если нужно purchaseDate:
        // editDto.setPurchaseDate(adminDto.getPurchaseDate()); // Предполагается, что в AdminTripDto есть геттер purchaseDate
        return editDto;
    }

    // Проверка, есть ли билет в этом DTO
    public boolean hasTicket() {
        return this.ticketId != null && this.ticketId > 0;
    }
}