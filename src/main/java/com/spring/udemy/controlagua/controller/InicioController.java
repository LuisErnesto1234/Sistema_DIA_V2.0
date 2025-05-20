package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.EsperaUsuarioControl;
import com.spring.udemy.controlagua.service.EsperaUsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/")
public class InicioController {

    private final EsperaUsuarioService esperaUsuarioService;

    public InicioController(EsperaUsuarioService esperaUsuarioService) {
        this.esperaUsuarioService = esperaUsuarioService;
    }

    @GetMapping
    public String inicio(){
        return "index";
    }


    @GetMapping("/inicio")
    public String mostrarInicio(Model model) {
        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<EsperaUsuarioControl> espera = esperaUsuarioService.listaEsperaUsuario();

        EsperaUsuarioControl usuarioJuntando = espera.stream()
                .filter(e -> "si".equals(e.getEstado()))
                .findFirst()
                .orElse(null);

        model.addAttribute("espera", espera);
        model.addAttribute("usuarioJuntando", usuarioJuntando);
        model.addAttribute("fecha", fecha.format(formato));

        return "inicio";
    }

    @GetMapping("/prueba")
    public String mostrarPrueba() {
        return "prueba";
    }

}
