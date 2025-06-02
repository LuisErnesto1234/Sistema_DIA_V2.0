package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.HistorialControles;
import com.spring.udemy.controlagua.repository.HistorialControlAguaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/historial")
public class HistorialControlesController {

    private final HistorialControlAguaRepository historialControlAguaRepository;

    public HistorialControlesController(HistorialControlAguaRepository historialControlAguaRepository) {
        this.historialControlAguaRepository = historialControlAguaRepository;
    }

    @GetMapping
    public String listarHistorial(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model){

        Pageable paginable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<HistorialControles> historialControles = historialControlAguaRepository.findAll(paginable);

        model.addAttribute("historialesPage", historialControles);
        model.addAttribute("paginaActual", page);
        return "control/historial";
    }

    @GetMapping("/{id}/control")
    public String listarHistorialPorControl(@PathVariable Long id, Model model){
        List<HistorialControles> historialControles = historialControlAguaRepository.findAll()
                .stream()
                .filter(h -> h.getIdControlModificado().equals(id))
                .toList();

        model.addAttribute("historiales", historialControles);

        return "control/historial";
    }
}
