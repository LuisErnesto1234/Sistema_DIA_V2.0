package com.spring.udemy.controlagua.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EsperaUsuarioControl {
    private Integer numeroOrden;
    private Usuario usuario;
    private String estado; // "si" o "no"
    private LocalDateTime horaInicio; // cuando empezó a usar el grifo
}

