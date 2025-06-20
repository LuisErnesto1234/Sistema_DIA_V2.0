package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String mostrarPerfil(Model model, Authentication authentication){
        String correo = authentication.getName();
        model.addAttribute("usuario", usuarioService.buscarUsuarioPorCorreo(correo).orElseThrow());
        return "perfil/perfil";
    }

    @GetMapping("/editar")
    public String editarPerfil(Model model, Authentication auth) {
        String correo = auth.getName();
        Usuario usuario = usuarioService.buscarUsuarioPorCorreo(correo).orElse(null);
        model.addAttribute("usuario", usuario);
        return "perfil/editar";
    }

    @PostMapping
    public String actualizarPerfil(@ModelAttribute Usuario usuarioForm, Authentication auth, RedirectAttributes attr,
                                   @RequestParam(name = "file") MultipartFile file) throws Exception {
        String correo = auth.getName();
        Usuario usuarioEnSesion = usuarioService.buscarUsuarioPorCorreo(correo).orElseThrow();

        // Verifica que sea el mismo usuario
        if (!usuarioForm.getId().equals(usuarioEnSesion.getId())) {
            attr.addFlashAttribute("error", "No puedes editar el perfil de otro usuario.");
            return "redirect:/perfil";
        }

        // Mantener el correo original del usuario en sesión
        usuarioForm.setCorreo(usuarioEnSesion.getCorreo());

        usuarioService.guardarUsuario(usuarioForm, file);
        attr.addFlashAttribute("success", "Perfil actualizado correctamente.");
        return "redirect:/perfil";
    }
}
