package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.PdfGeneratorService;
import com.spring.udemy.controlagua.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarUsuarioPorId(id).orElseThrow();

        Map<String, Object> data = new HashMap<>();
        data.put("usuario", usuario);

        model.addAttribute("usuario", usuario);

        byte[] pdfBytes = pdfGeneratorService.generatePdf("usuario-pdf", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "usuario_" + usuario.getNombre() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

}
