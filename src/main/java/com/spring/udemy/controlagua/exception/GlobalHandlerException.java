package com.spring.udemy.controlagua.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalHandlerException {

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

    @ExceptionHandler(Exception.class)
    public String manejarExcepcionGeneral(Exception ex, Model model) {
        model.addAttribute("error", "Ocurrió un error inesperado");
        model.addAttribute("mensaje", ex.getMessage());
        return "error/general";
    }
}
