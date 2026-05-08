package com.gestion.gestion_bus.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {
    public Long userId;
    public String oldPassword;
    public String newPassword;
}
