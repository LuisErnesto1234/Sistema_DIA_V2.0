package com.spring.udemy.controlagua.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "control")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ControlAgua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fechaRegistro;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    private Integer minutosUtilizados;
}
