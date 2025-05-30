package com.spring.udemy.controlagua.exception;

import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.UsuarioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalHandlerException {

    private final UsuarioService usuarioService;

    public GlobalHandlerException(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFoundException(NoHandlerFoundException e, Model model){
        model.addAttribute("error", "Página no encontrada");
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String manejarValidacionFallida(MethodArgumentNotValidException ex, Model model) {
        model.addAttribute("error", "Error de validación en los datos del formulario");
        return "error/validacion";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String manejarViolacionIntegridadDatos(Exception ex, Model model) {
        model.addAttribute("error", "Violación de integridad de datos");
        return "error/integridad";
    }

//    @ExceptionHandler(Exception.class)
//    public String manejarExcepcionGeneral(Exception ex, Model model) {
//        model.addAttribute("error", "Ocurrió un error inesperado");
//        model.addAttribute("mensaje", ex.getMessage());
//        return "error/general";
//    }

    @ModelAttribute("usuarioLogeado")
    public Usuario mostrarPerfilSide(Authentication authentication){

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String correo = authentication.getName();
        return usuarioService.buscarUsuarioPorCorreo(correo).orElse(null);
    }
//
//    @ModelAttribute("currentUser")
//    public Usuario getCurrentUser(Authentication authentication) {
//        // Verificar primero si authentication es null
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return null;
//        }
//
//        // Ahora podemos acceder con seguridad al principal
//        Object principal = authentication.getPrincipal();
//
//        // Verificar si el principal es de tipo Usuario
//        if (principal instanceof Usuario) {
//            return (Usuario) principal;
//        }
//
//        return null;
//    }
}
