package com.spring.udemy.controlagua.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.model.ControlAguaDTO;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.ControlAguaService;
import com.spring.udemy.controlagua.service.UsuarioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        model.addAttribute("control", controlAguas);
        model.addAttribute("usuario", usuarioService.buscarUsuarioPorId(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado")));
        // En tu controlador
        int startPage = Math.max(0, page - 1);
        int endPage = Math.min(page + 1, controlAguas.getTotalPages() - 1);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("paginaActual", page);
        model.addAttribute("minutosUsados", controlAguaService.controlAguaList().stream().
                filter(c -> c.getUsuario().getId().equals(id)).
                mapToInt(ControlAgua::getMinutosUtilizados).sum());
        return "reportes/usuario";
    }

    @GetMapping("/usuario/{id}/exportar")
    public ResponseEntity<Resource> exportarUsuario(@PathVariable Long id) throws IOException {
        // Obtener controles
        List<ControlAgua> controles = controlAguaService.listarControlesPorUsuario(id);

        // Obtener nombre del usuario
        Usuario usuario = usuarioService.buscarUsuarioPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String nombreUsuario = usuario.getNombre() + "_" + usuario.getApellido();

        // Convertir controles a DTOs
        List<ControlAguaDTO> lista = controles.stream().map(control -> {
            ControlAguaDTO dto = new ControlAguaDTO();
            dto.setId(control.getId());
            dto.setFechaRegistro(control.getFechaRegistro());
            dto.setHoraInicio(control.getHoraInicio());
            dto.setHoraFin(control.getHoraFin());
            dto.setMinutosUtilizados(control.getMinutosUtilizados());
            dto.setTipoRegistro(control.getTipoRegistro());
            dto.setPrecio(control.getPrecio());
            return dto;
        }).toList();

        // Crear archivo temporal JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Soporte para LocalDate y LocalTime

        File archivo = new File("backup_" + nombreUsuario + ".json");
        mapper.writeValue(archivo, lista); // ¡Aquí pasas 'lista' como contenido!

        // Devolver como recurso
        InputStreamResource resource = new InputStreamResource(new FileInputStream(archivo));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + archivo.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(archivo.length())
                .body(resource);
    }

    @PostMapping("/usuario/{id}/importar")
    public String importarBackup(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo, RedirectAttributes redirectAttributes) {
        try {
            // Buscar el usuario
            Usuario usuario = usuarioService.buscarUsuarioPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Leer el JSON y convertirlo en lista de DTOs
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            List<ControlAguaDTO> controlesDTO = Arrays.asList(
                    mapper.readValue(archivo.getInputStream(), ControlAguaDTO[].class)
            );

            // Convertir a entidades y asociar con el usuario
            List<ControlAgua> controles = controlesDTO.stream().map(dto -> {
                ControlAgua control = new ControlAgua();
                control.setUsuario(usuario);
                control.setFechaRegistro(dto.getFechaRegistro());
                control.setHoraInicio(dto.getHoraInicio());
                control.setHoraFin(dto.getHoraFin());
                control.setMinutosUtilizados(dto.getMinutosUtilizados());
                control.setTipoRegistro(dto.getTipoRegistro());
                control.setPrecio(dto.getPrecio());
                return control;
            }).toList();

            // Guardar en la base de datos
            controlAguaService.guardarTodos(controles); // Este método lo vemos abajo si no lo tienes

            redirectAttributes.addFlashAttribute("success", "Backup importado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al importar backup: " + e.getMessage());
        }

        return "redirect:/reportes/usuario/" + id; // o donde muestres los usuarios
    }




}
