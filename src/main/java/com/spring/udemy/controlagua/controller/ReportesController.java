package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.service.ControlAguaService;
import com.spring.udemy.controlagua.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    private final ControlAguaService controlAguaService;
    private final UsuarioService usuarioService;

    public ReportesController(ControlAguaService controlAguaService, UsuarioService usuarioService) {
        this.controlAguaService = controlAguaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String mostrarVistaInicial(Model model) {
        model.addAttribute("controles", Page.empty());
        model.addAttribute("paginaActual", 0);
        model.addAttribute("fecha", null);
        return "reportes/filtrar";
    }

    @GetMapping(params = {"fecha", "page"})
    public String paginarPorFecha(@RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                  @RequestParam("page") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model) {
        Pageable paginable = PageRequest.of(page, size, Sort.by("horaInicio").ascending());
        Page<ControlAgua> controlAguas = controlAguaService.buscarControlPorFecha(fecha, paginable);

        model.addAttribute("controles", controlAguas);
        model.addAttribute("paginaActual", page);
        model.addAttribute("fecha", fecha);
        return "reportes/filtrar";
    }


    @PostMapping
    public String filtrarPorFecha(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size, Model model,
                                  @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha){
        Pageable paginable = PageRequest.of(page, size, Sort.by("horaInicio").ascending());
        Page<ControlAgua> controlAguas = controlAguaService.buscarControlPorFecha(fecha, paginable);
        model.addAttribute("controles", controlAguas);
        model.addAttribute("paginaActual", page);
        model.addAttribute("fecha", fecha);
        return "redirect:/reportes?fecha=" + fecha + "&page=0";
    }

    @GetMapping("/usuario/{id}")
    public String filtrarControlesPorUsuario(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int size, Model model){

        Pageable paginable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        Page<ControlAgua> controlAguas = controlAguaService.buscarControlPorIdUsuario(id, paginable);
        model.addAttribute("controles", controlAguas);
        model.addAttribute("usuario", usuarioService.buscarUsuarioPorId(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado")));
        model.addAttribute("paginaActual", page);
        model.addAttribute("minutosUsados", controlAguaService.controlAguaList().stream().
                filter(c -> c.getUsuario().getId().equals(id)).
                mapToInt(ControlAgua::getMinutosUtilizados).sum());
        return "reportes/usuario";
    }



}
