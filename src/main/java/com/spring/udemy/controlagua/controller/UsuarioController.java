package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.model.UsuarioDTO;
import com.spring.udemy.controlagua.service.PdfGeneratorService;
import com.spring.udemy.controlagua.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PdfGeneratorService pdfGeneratorService;

    public UsuarioController(UsuarioService usuarioService, PdfGeneratorService pdfGeneratorService) {
        this.usuarioService = usuarioService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @GetMapping
    public String listarUsuarios(Model model, @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size){

        Pageable paginable = PageRequest.of(page, size, Sort.by("apellido").ascending());
        Page<Usuario> usuariosPage = usuarioService.listarUsuarios(paginable);
        model.addAttribute("usuarios", usuariosPage);
        model.addAttribute("paginaActual", page);
        return "usuario/listar";
    }

    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<UsuarioDTO>> buscarUsuariosPorNombreOApellido(@RequestParam String query) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Usuario> usuarios = usuarioService
                .buscarPorNombreOApellido(query, pageable)
                .getContent();

        List<UsuarioDTO> usuariosDTO = usuarios.stream()
                .map(u -> new UsuarioDTO(u.getId(), u.getNombre(), u.getApellido(), u.getMinutosAcumulados(), u.getMinutosSemana(), u.getFotoPerfil(), u.getRoles()))
                .toList();

        return ResponseEntity.ok(usuariosDTO);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editarUsuario(Model model, @PathVariable Long id){
        model.addAttribute("usuario", usuarioService.buscarUsuarioPorId(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado")));
        return "usuario/formulario";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model){
        model.addAttribute("usuario", new Usuario());
        return "usuario/formulario";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id){
        if (id != null){
            Usuario usuario = usuarioService.buscarUsuarioPorId(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            if (usuario.getFotoPerfil() != null){
                if (usuario.getFotoPerfil().equals("/uploads/perfil/default.png")){
                    usuarioService.eliminarUsuario(id);
                }
                else {
                    String rutaBase = System.getProperty("user.dir");
                    Path rutaImagen = Paths.get(rutaBase + usuario.getFotoPerfil());
                    File file = rutaImagen.toFile();
                    if (file.exists()) {
                        boolean eliminar = file.delete();
                        if (!eliminar) {
                            throw new RuntimeException("No se pudo eliminar la imagen");
                        }
                    }
                    usuarioService.eliminarUsuario(id);
                }
            }
        }

        return "redirect:/usuario";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardarUsuario(@Valid @ModelAttribute Usuario usuario, BindingResult bindingResult,
                                 @RequestParam(name = "imagen", required = false) MultipartFile file) throws IOException {
        if (bindingResult.hasErrors()) {
            return "usuario/formulario";
        }
        usuarioService.guardarUsuario(usuario, file);
        return "redirect:/usuario";
    }

    @GetMapping("/generar-pdf")
    public ResponseEntity<byte[]> generarPdfListadoUsuarios(){

        try {
            List<Usuario> usuarioList = usuarioService.listaUsuario();

            //Preparamos el contexto para thymeleaf
            Context context = new Context();
            context.setVariable("usuarios", usuarioList);
            context.setVariable("title", "Lista de usuarios registrados");

            byte[] pdfBytes = pdfGeneratorService.generatePdf("usuarios-pdf", context);

            //Configurar la respuesta HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String fileName = "usuarios-registrados.pdf";
            headers.setContentDispositionFormData("filename", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error al generar PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
