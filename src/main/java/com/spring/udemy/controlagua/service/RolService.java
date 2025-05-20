package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.Rol;
import com.spring.udemy.controlagua.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public Optional<Rol> getRolByNombre(String nombre){
        return rolRepository.findByNombre(nombre);
    }

}
