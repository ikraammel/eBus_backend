package com.gestion.gestion_bus.service.interfaces;


import com.gestion.gestion_bus.dto.CreateTicketDto;
import com.gestion.gestion_bus.dto.TicketDto;

import java.util.List;

public interface TicketService {

    List<TicketDto> getAllTickets();
    TicketDto getTicketById(Long id);
    TicketDto createTicket(CreateTicketDto dto);
    TicketDto updateTicket(Long id, CreateTicketDto dto);
    void deleteTicket(Long id);
}