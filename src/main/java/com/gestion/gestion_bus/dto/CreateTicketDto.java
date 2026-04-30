package com.gestion.gestion_bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketDto{
    private double prix;
    private String typeTicket;
}
