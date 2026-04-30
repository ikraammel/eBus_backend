package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.CreateTicketDto;
import com.gestion.gestion_bus.dto.TicketDto;
import com.gestion.gestion_bus.enums.TypeTicket;
import com.gestion.gestion_bus.mappers.TicketMapper;
import com.gestion.gestion_bus.models.Ticket;
import com.gestion.gestion_bus.repository.TicketRepository;
import com.gestion.gestion_bus.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TicketServiceImp implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public List<TicketDto> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketDto getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return TicketMapper.mapToDTO(ticket);
    }

    @Override
    public TicketDto createTicket(CreateTicketDto dto) {
        Ticket ticket = TicketMapper.mapToEntity(dto);
        return TicketMapper.mapToDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketDto updateTicket(Long id, CreateTicketDto dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setPrix(dto.getPrix());
        ticket.setTypeTicket(TypeTicket.valueOf(dto.getTypeTicket()));
        return TicketMapper.mapToDTO(ticketRepository.save(ticket));
    }

    @Override
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}