package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.CreateTicketDto;
import com.gestion.gestion_bus.dto.TicketDto;
import com.gestion.gestion_bus.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketDto> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketDto getTicket(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping
    public TicketDto createTicket(@RequestBody CreateTicketDto dto) {
        return ticketService.createTicket(dto);
    }

    @PutMapping("/{id}")
    public TicketDto updateTicket(@PathVariable Long id,
                                  @RequestBody CreateTicketDto dto) {
        return ticketService.updateTicket(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
    }
}