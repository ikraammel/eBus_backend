package com.gestion.gestion_bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankCardDto {
    private Long id;
    private String last4;
    private String holder;
    private String expiry;
    private int colorIndex;
    private boolean isMain;
    private Long userId;
}
