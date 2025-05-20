package com.spring.udemy.controlagua.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorDTO {
    private String status;
    private String mensaje;
    private LocalDateTime timestamp;
    private List<MensajeError> mensajeErrors;
}
