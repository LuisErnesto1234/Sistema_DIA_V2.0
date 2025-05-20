package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.ControlAguaService;
import com.spring.udemy.controlagua.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/control")
public class ControlAguaController {

    private final ControlAguaService controlAguaService;
    private final UsuarioService usuarioService;

    public ControlAguaController(ControlAguaService controlAguaService, UsuarioService usuarioService) {
        this.controlAguaService = controlAguaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarControles(Model model, @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        Page<ControlAgua> controlesPage = controlAguaService.listarControles(pageable);

        model.addAttribute("controlesPage", controlesPage);
        model.addAttribute("paginaActual", page);

        return "control/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarControlAgua(@PathVariable Long id){
        controlAguaService.eliminarControlAgua(id);
        return "redirect:/control";
    }

    @GetMapping("/editar/{id}")
    public String editarControlAgua(Model model, @PathVariable Long id){
        model.addAttribute("control", controlAguaService.buscarControlAguaPorId(id));
        model.addAttribute("usuarios", usuarioService.listaUsuario());
        return "control/formulario";
    }

    @GetMapping("/nuevo")
    public String nuevoControlAgua(Model model) {
        ControlAgua controlAgua = new ControlAgua();
        controlAgua.setFechaRegistro(LocalDate.now());

        model.addAttribute("control", controlAgua);
        model.addAttribute("usuarios", usuarioService.listaUsuario());
        return "control/formulario";
    }

    @PostMapping("/guardar")
    public String guardarControlAgua(@ModelAttribute("control") @Valid ControlAgua controlAgua,
                                     BindingResult bindingResult,
                                     Model model, RedirectAttributes attr) {

        // Validar horas manualmente ANTES de verificar hasErrors()
        if (controlAgua.getHoraInicio() == null) {
            bindingResult.rejectValue("horaInicio", "field.required", "La hora de inicio es obligatoria.");
        }
        if (controlAgua.getHoraFin() == null) {
            bindingResult.rejectValue("horaFin", "field.required", "La hora de fin es obligatoria.");
        }

        if (controlAgua.getHoraInicio() != null && controlAgua.getHoraFin() != null && controlAgua.getHoraFin().isBefore(controlAgua.getHoraInicio())) {
            bindingResult.rejectValue("horaFin", "field.invalid", "La hora de fin debe ser después de la hora de inicio.");
        }

        // Verificar si hubo errores (ya sean por anotaciones @NotNull o rejectValue manual)
        if (bindingResult.hasErrors()) {
            model.addAttribute("control", controlAgua);
            model.addAttribute("controles", controlAguaService.controlAguaList());
            model.addAttribute("usuarios", usuarioService.listaUsuario());
            return "control/formulario";
        }

        //TODO Cálculo de minutos si tod es válido

        Usuario usuario = usuarioService.buscarUsuarioPorId(controlAgua.getUsuario().getId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Integer minutosUtilizados = controlAguaService.cantidadMinutosAcumulados(controlAgua.getHoraInicio(), controlAgua.getHoraFin());
        System.out.println("Minutos utilizados" + minutosUtilizados);
        usuarioService.MinutosUtilizadosOperacion(minutosUtilizados, usuario);
        //TODO Test

        //TODO Actualizar datos en el control
        controlAgua.setUsuario(usuario);
        controlAgua.setMinutosUtilizados(minutosUtilizados);

        //TODO Guardar el Control
        attr.addFlashAttribute("success", "Control aguadado correctamente.");
        controlAguaService.guardarControlAgua(controlAgua);
        return "redirect:/control";
    }

    @GetMapping("/export/excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=controles.xlsx");

        List<ControlAgua> controles = controlAguaService.controlAguaList();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Controles");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Usuario");
        header.createCell(1).setCellValue("Fecha");
        header.createCell(2).setCellValue("Hora InicioController");
        header.createCell(3).setCellValue("Hora Fin");
        header.createCell(4).setCellValue("Minutos");

        int rowNum = 1;
        for (ControlAgua c : controles) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getUsuario().getNombre());
            row.createCell(1).setCellValue(c.getFechaRegistro().toString());
            row.createCell(2).setCellValue(c.getHoraInicio().toString());
            row.createCell(3).setCellValue(c.getHoraFin().toString());
            row.createCell(4).setCellValue(c.getMinutosUtilizados());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
