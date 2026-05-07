package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.CreateTicketDto;
import com.gestion.gestion_bus.dto.TicketDto;
import com.gestion.gestion_bus.models.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {
    public static TicketDto mapToDTO(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setPrix(ticket.getPrix());
        dto.setTypeTicket(ticket.getTypeTicket());
        return dto;
    }

    public static Ticket mapToEntity(CreateTicketDto dto) {
        Ticket ticket = new Ticket();
        ticket.setPrix(dto.getPrix());
        ticket.setTypeTicket(dto.getTypeTicket());
        return ticket;
    }
}
