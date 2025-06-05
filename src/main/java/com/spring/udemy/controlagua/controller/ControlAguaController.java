package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.model.TipoRegistro;
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
import java.time.LocalTime;
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
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ControlAgua> controlesPage = controlAguaService.listarControles(pageable);

        model.addAttribute("controlesPage", controlesPage);
        model.addAttribute("paginaActual", page);

        return "control/listar";
    }

    @GetMapping("/recarga/editar/{id}")
    public String editarControlAguaRecarga(Model model, @PathVariable Long id){
        ControlAgua control = controlAguaService.buscarControlAguaPorId(id);
        Usuario usuario = usuarioService.buscarUsuarioPorId(control.getUsuario().getId()).orElseThrow(() ->
                new RuntimeException("No existe el usuario"));
        model.addAttribute("control", control);
        model.addAttribute("usuario", usuario);
        return "control/formulario-recarga";
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

    @GetMapping("/recarga")
    public String controlRecarga(@ModelAttribute("control") ControlAgua recarga, Model model) {
        if (recarga == null || recarga.getUsuario() == null) {
            return "redirect:/control"; // Si alguien entra directo sin pasar por el flujo correcto
        }

        Usuario usuario = usuarioService.buscarUsuarioPorId(recarga.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado!"));

        recarga.setUsuario(usuario);

        model.addAttribute("control", recarga);
        model.addAttribute("usuario", usuario);

        return "control/formulario-recarga";
    }

    @PostMapping("/recarga/guardar")
    public String guardarControlAguaRecarga(@ModelAttribute("control") @Valid ControlAgua controlAgua,
                                            BindingResult bindingResult,
                                            Model model,
                                            RedirectAttributes attr) {

        validarHoras(controlAgua, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, controlAgua);
            return "control/formulario-recarga";
        }

        // Ya guarda y devuelve el tipo dentro de este método
        TipoRegistro tipoRegistro = controlAguaService.procesarControl(controlAgua);

        // ¡Ya no volvemos a guardar el control! porque ya lo hizo `procesarControl()`

        attr.addFlashAttribute("success", "Control por recarga guardado correctamente!");
        return "redirect:/control";
    }

    @PostMapping("/guardar")
    public String guardarControlAguaUso(@ModelAttribute("control") @Valid ControlAgua controlAgua,
                                        BindingResult bindingResult,
                                        Model model, RedirectAttributes attr) {

        validarHoras(controlAgua, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, controlAgua);
            return "control/formulario";
        }

        TipoRegistro tipoRegistro;

        if (controlAgua.getId() != null) {
            // Es una edición
            tipoRegistro = controlAguaService.editarControlAgua(controlAgua.getId(), controlAgua);
            attr.addFlashAttribute("success", "Control editado correctamente.");
        } else {
            // Es un registro nuevo
            tipoRegistro = controlAguaService.procesarControl(controlAgua);
            attr.addFlashAttribute("success", "Control guardado correctamente.");
        }

        if (tipoRegistro == TipoRegistro.RECARGA) {
            attr.addFlashAttribute("control", controlAgua);  // Pasa el objeto completo
            return "redirect:/control/recarga";
        }


        return "redirect:/control";
    }

    private void prepararFormulario(Model model, ControlAgua controlAgua) {
        model.addAttribute("control", controlAgua);
        model.addAttribute("controles", controlAguaService.controlAguaList());
        model.addAttribute("usuarios", usuarioService.listaUsuario());
    }

    private void validarHoras(ControlAgua controlAgua, BindingResult bindingResult){
        if (controlAgua.getHoraInicio() == null) {
            bindingResult.rejectValue("horaInicio", "field.required", "La hora de inicio es obligatoria.");
        }
        if (controlAgua.getHoraFin() == null) {
            bindingResult.rejectValue("horaFin", "field.required", "La hora de fin es obligatoria.");
        }

        if (controlAgua.getHoraInicio() != null && controlAgua.getHoraFin() != null && controlAgua.getHoraFin().isBefore(controlAgua.getHoraInicio())) {
            bindingResult.rejectValue("horaFin", "field.invalid", "La hora de fin debe ser después de la hora de inicio.");
        }
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
