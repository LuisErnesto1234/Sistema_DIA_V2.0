package com.spring.udemy.controlagua.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ControlAguaDTO {
    private Long id;
    private LocalDate fechaRegistro;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer minutosUtilizados;
    private TipoRegistro tipoRegistro; //USO o RECARGA
    private Double precio; //Solo para recargas
}
