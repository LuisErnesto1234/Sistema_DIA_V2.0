package com.spring.udemy.controlagua.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HistorialControles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaCambio;

    private Integer minutosAntes;
    private Integer minutosDespues;

    private String observacion;

    @Enumerated(EnumType.STRING)
    private TipoAccion accion;

    @ManyToOne
    private Usuario usuarioQueModifico;

    // Usa esto:
    private Long idControlModificado;
}
