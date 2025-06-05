package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.*;
import com.spring.udemy.controlagua.repository.ControlAguaRepository;
import com.spring.udemy.controlagua.repository.HistorialControlAguaRepository;
import com.spring.udemy.controlagua.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class ControlAguaService {

    private final ControlAguaRepository controlAguaRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final HistorialControlAguaRepository historialRepository;

    public ControlAguaService(ControlAguaRepository controlAguaRepository, HistorialControlAguaRepository historialRepository, UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.controlAguaRepository = controlAguaRepository;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
    }

    public List<ControlAgua> controlAguaList(){
        return Collections.unmodifiableList(controlAguaRepository.findAll());
    }

    @Transactional
    public void eliminarControlAgua(Long id){
        ControlAgua control = controlAguaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control no encontrado"));

        devolverMinutosEliminar(control);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        Usuario usuarioSesion = usuarioService.buscarUsuarioPorCorreo(correo).orElseThrow();

        HistorialControles historial = new HistorialControles();
        historial.setFechaCambio(LocalDateTime.now());
        historial.setAccion(TipoAccion.ELIMINACION);
        historial.setMinutosAntes(control.getMinutosUtilizados());
        historial.setMinutosDespues(0);
        historial.setObservacion("Control eliminado del usuario: " + control.getUsuario().getNombre() +
                " " + control.getUsuario().getApellido());
        historial.setIdControlModificado(control.getId());
        historial.setUsuarioQueModifico(usuarioSesion);

        historialRepository.save(historial);
        controlAguaRepository.deleteById(id);
    }

    public void devolverMinutosEliminar(ControlAgua controlAguaActualizado){
        if (controlAguaActualizado.getTipoRegistro() == TipoRegistro.USO){
            Usuario usuario = controlAguaActualizado.getUsuario();
            int minutosUsados = controlAguaActualizado.getMinutosUtilizados();

            int minutosSemana = usuario.getMinutosSemana() != null ? usuario.getMinutosSemana() : 0;
            int minutosAcumulados = usuario.getMinutosAcumulados() != null ? usuario.getMinutosAcumulados() : 0;


            if (minutosSemana < 60){
                //TODO: Devolver al usuario minutos usados en minutos de semana
                int espacioDisponible = 60 - minutosSemana;
                int aSumarSemana = Math.min(minutosUsados, espacioDisponible);
                int aSumarAcumulados = minutosUsados - aSumarSemana;

                usuario.setMinutosSemana(minutosSemana + aSumarSemana);
                usuario.setMinutosAcumulados(minutosAcumulados + aSumarAcumulados);
            }else {
                usuario.setMinutosAcumulados(minutosAcumulados + minutosUsados);
            }
            usuarioRepository.save(usuario);
        }
    }

    public void guardarControlAgua(ControlAgua controlAgua){
        controlAguaRepository.save(controlAgua);
    }

    public Integer cantidadMinutosUsados(LocalTime horaInicio, LocalTime horaFin){
        return (Integer) (int) Duration.between(horaInicio, horaFin).toMinutes();
    }

    public TipoRegistro procesarControl(ControlAgua controlAgua) {
        // Obtener el usuario existente desde la base de datos
        Usuario usuario = usuarioService.buscarUsuarioPorId(controlAgua.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Calcular minutos utilizados
        int minutos = cantidadMinutosUsados(controlAgua.getHoraInicio(), controlAgua.getHoraFin());
        controlAgua.setMinutosUtilizados(minutos);
        controlAgua.setUsuario(usuario);

        // Determinar el tipo de registro y aplicar lógica de minutos
        usuarioService.MinutosUtilizadosOperacion(minutos, usuario, controlAgua);

        // Si es un uso, fijar precio 0
        if (controlAgua.getTipoRegistro() == TipoRegistro.USO) {
            controlAgua.setPrecio(0.0);
        }

        // Guardar el control primero para obtener el ID
        guardarControlAgua(controlAgua); // <- aquí se guarda en BD

        // Obtener el usuario autenticado desde el contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        Usuario usuarioSesion = usuarioService.buscarUsuarioPorCorreo(correo).orElseThrow();

        // Crear y guardar el historial
        HistorialControles historial = new HistorialControles();
        historial.setFechaCambio(LocalDateTime.now());
        historial.setAccion(TipoAccion.CREACION);
        historial.setMinutosAntes(0); // Como es nuevo, antes era 0
        historial.setMinutosDespues(minutos);
        if(controlAgua.getTipoRegistro() == TipoRegistro.RECARGA){
            historial.setObservacion("Nuevo control creado por recarga, al usuario: " +
                    controlAgua.getUsuario().getNombre() + " " + controlAgua.getUsuario().getApellido());
        }else {
            historial.setObservacion("Nuevo control creado por consumo, al usuario: " +
                            controlAgua.getUsuario().getNombre() + " " + controlAgua.getUsuario().getApellido());
        }
        historial.setIdControlModificado(controlAgua.getId()); // Ya persistido
        historial.setUsuarioQueModifico(usuarioSesion);

        historialRepository.save(historial); // <- ahora sí, sin error

        return controlAgua.getTipoRegistro();
    }

    @Transactional
    public TipoRegistro editarControlAgua(Long idControl, ControlAgua datosActualizados) {
        ControlAgua original = controlAguaRepository.findById(idControl)
                .orElseThrow(() -> new RuntimeException("Control no encontrado"));

        Usuario usuario = usuarioService.buscarUsuarioPorId(original.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int minutosControlOriginal = original.getMinutosUtilizados();
        int minutosControlActualizado = cantidadMinutosUsados(datosActualizados.getHoraInicio(), datosActualizados.getHoraFin());

        if (original.getTipoRegistro() == TipoRegistro.RECARGA) {
            // Solo actualizamos horas y precio, sin tocar minutos del usuario
            original.setHoraInicio(datosActualizados.getHoraInicio());
            original.setHoraFin(datosActualizados.getHoraFin());
            original.setMinutosUtilizados(minutosControlActualizado);
            original.setPrecio(minutosControlActualizado * 0.5);
            controlAguaRepository.save(original);
            return TipoRegistro.RECARGA;
        }

        int diferencia = minutosControlOriginal - minutosControlActualizado;
        AjustarMinutosPorEdicion(diferencia, usuario);

        // Actualiza el control
        original.setHoraInicio(datosActualizados.getHoraInicio());
        original.setHoraFin(datosActualizados.getHoraFin());
        original.setMinutosUtilizados(minutosControlActualizado);
        controlAguaRepository.save(original);

        // Crear historial
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        Usuario usuarioQueModifico = usuarioService.buscarUsuarioPorCorreo(correo).orElseThrow();

        HistorialControles historial = new HistorialControles();
        historial.setFechaCambio(LocalDateTime.now());
        historial.setMinutosAntes(minutosControlOriginal);
        historial.setMinutosDespues(minutosControlActualizado);
        historial.setUsuarioQueModifico(usuarioQueModifico);
        historial.setIdControlModificado(original.getId());
        historial.setAccion(TipoAccion.EDICION);
        historial.setObservacion("Edición de control por diferencia de " + diferencia + " minutos." +
                "Realizado para el usuario: " + original.getUsuario().getNombre() + " " + original.getUsuario().getApellido());
        historialRepository.save(historial);
        usuarioRepository.save(usuario);

        return original.getTipoRegistro();
    }

    public void AjustarMinutosPorEdicion(int diferencia, Usuario usuario) {
        if (usuario == null || diferencia == 0) return;

        if (diferencia > 0) {
            // Se usaron menos minutos al editar → devolver minutos al usuario
            // Devolver primero a acumulados si semana está en 0
            if (usuario.getMinutosSemana() == 0) {
                usuario.setMinutosAcumulados(usuario.getMinutosAcumulados() + diferencia);
            } else {
                usuario.setMinutosSemana(usuario.getMinutosSemana() + diferencia);
            }
        } else {
            // Se usaron más minutos al editar → quitar más minutos
            int extraNecesarios = Math.abs(diferencia);

            if (usuario.getMinutosSemana() >= extraNecesarios) {
                usuario.setMinutosSemana(usuario.getMinutosSemana() - extraNecesarios);
            } else {
                int resto = extraNecesarios - usuario.getMinutosSemana();
                usuario.setMinutosSemana(0);
                usuario.setMinutosAcumulados(Math.max(0, usuario.getMinutosAcumulados() - resto));
            }
        }
    }

    public ControlAgua buscarControlAguaPorId(Long id){
        return controlAguaRepository.findById(id).orElse(null);
    }

    public Page<ControlAgua> listarControles(Pageable pageable) {
        return controlAguaRepository.findAll(pageable);
    }

    public Page<ControlAgua> buscarControlPorIdUsuario(Long idUsuario, Pageable pageable){
        return controlAguaRepository.findByUsuarioId(idUsuario, pageable);
    }

    public Page<ControlAgua> buscarControlPorFecha(LocalDate fecha, Pageable pageable){
        return controlAguaRepository.findByFechaRegistro(fecha, pageable);
    }

    public List<Integer> getAvailableYears() {
        return controlAguaRepository.findDistinctYears();
    }

    public List<Integer> getAvailableMonths(int year) {
        return controlAguaRepository.findDistinctMonthsByYear(year);
    }

    public List<Integer> getAvailableDays(int year, int month) {
        return controlAguaRepository.findDistinctDaysByYearAndMonth(year, month);
    }

    public Page<ControlAgua> filterByDate(Integer year, Integer month, Integer day, Pageable pageable) {
        if (day != null && month != null && year != null) {
            // Filtro por día específico
            LocalDate fecha = LocalDate.of(year, month, day);
            return controlAguaRepository.findByFechaRegistro(fecha, pageable);
        } else if (month != null && year != null) {
            // Filtro por mes
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            return controlAguaRepository.findByFechaRegistroBetween(startDate, endDate, pageable);
        } else if (year != null) {
            // Filtro por año
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            return controlAguaRepository.findByFechaRegistroBetween(startDate, endDate, pageable);
        } else {
            return controlAguaRepository.findAll(pageable);
        }
    }

}
