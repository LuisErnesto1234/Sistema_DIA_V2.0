package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.service.ControlAguaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/reporte/fecha")
public class FechasReportesController {

    private final ControlAguaService controlAguaService;

    public FechasReportesController(ControlAguaService controlAguaService) {
        this.controlAguaService = controlAguaService;
    }

    @GetMapping
    public String mostrarVistaInicial(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // Obtener años disponibles para el select
        List<Integer> availableYears = controlAguaService.getAvailableYears();
        model.addAttribute("availableYears", availableYears);

        // Inicializar listas vacías por defecto
        List<Integer> availableMonths = Collections.emptyList();
        List<Integer> availableDays = Collections.emptyList();

        // Obtener meses disponibles si hay año seleccionado
        if (year != null) {
            availableMonths = controlAguaService.getAvailableMonths(year);

            // Obtener días disponibles si hay año y mes seleccionado
            if (month != null) {
                availableDays = controlAguaService.getAvailableDays(year, month);
            }
        }

        // Agregar atributos al modelo
        model.addAttribute("availableMonths", availableMonths);
        model.addAttribute("availableDays", availableDays);

        // Resto de tu lógica (filtrado, paginación, etc.)
        Page<ControlAgua> controles = controlAguaService.filterByDate(year, month, day,
                PageRequest.of(page, size, Sort.by("fechaRegistro").descending()));

        model.addAttribute("controles", controles);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedDay", day);
        model.addAttribute("paginaActual", page);

        return "reportes/lista-u";
    }

    @PostMapping
    public String filtrar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        redirectAttributes.addAttribute("day", day);
        return "redirect:/reporte/fecha";
    }
}
