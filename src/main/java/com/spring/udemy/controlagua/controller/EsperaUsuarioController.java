package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.EsperaUsuarioControl;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.EsperaUsuarioService;
import com.spring.udemy.controlagua.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/espera")
public class EsperaUsuarioController {

    private final EsperaUsuarioService esperaUsuarioService;
    private final UsuarioService usuarioService;

    public EsperaUsuarioController(EsperaUsuarioService esperaUsuarioService, UsuarioService usuarioService) {
        this.esperaUsuarioService = esperaUsuarioService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/siguiente")
    public String pasarAlSiguiente(RedirectAttributes attr) {
        esperaUsuarioService.iniciarTurno();
        attr.addFlashAttribute("success", "Turno terminado correctamente.");
        return "redirect:/espera";
    }

    @GetMapping
    public String listarEsperaUsuarios(Model model){
        List<Usuario> todosUsuarios = usuarioService.listaUsuario();

        List<Usuario> usuariosEnEspera = esperaUsuarioService.listaEsperaUsuario().stream()
                .map(EsperaUsuarioControl::getUsuario)
                .toList();

        List<Usuario> usuariosDisponibles = todosUsuarios.stream()
                .filter(u -> !usuariosEnEspera.contains(u))
                .collect(Collectors.toList());

        model.addAttribute("esperaUsuarios", esperaUsuarioService.listaEsperaUsuario());
        model.addAttribute("usuarios", usuariosDisponibles);
        model.addAttribute("usuarioActual", esperaUsuarioService.getUsuarioActual());

        return "espera/fila";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEsperaUsuario(@PathVariable Integer id){
        EsperaUsuarioControl esperaUsuarioControl = esperaUsuarioService.buscarUsuarioPorNumeroOrden(id);
        esperaUsuarioService.eliminarUsuario(esperaUsuarioControl);
        return "redirect:/espera";
    }

    @PostMapping("/registrar")
    public String ponerEnEsperaUsuario(@RequestParam Long usuarioId, RedirectAttributes attr){
        Usuario usuario = usuarioService.buscarUsuarioPorId(usuarioId).orElseThrow(); // Asume que este método existe
        EsperaUsuarioControl espera = new EsperaUsuarioControl();
        espera.setUsuario(usuario);
        esperaUsuarioService.agregarUsuario(espera);
        attr.addFlashAttribute("success", "Usuario agregado correctamente.");
        return "redirect:/espera";
    }

}
