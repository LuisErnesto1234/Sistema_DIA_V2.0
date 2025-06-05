package com.spring.udemy.controlagua.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private Integer minutosAcumulados;
    private Integer minutosSemana;
    private String fotoPerfil;
    private Set<Rol> roles = new HashSet<>();
}

