package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.repository.ControlAguaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class ControlAguaService {

    private final ControlAguaRepository controlAguaRepository;

    public ControlAguaService(ControlAguaRepository controlAguaRepository) {
        this.controlAguaRepository = controlAguaRepository;
    }

    public List<ControlAgua> controlAguaList(){
        return Collections.unmodifiableList(controlAguaRepository.findAll());
    }

    public void eliminarControlAgua(Long id){
        controlAguaRepository.deleteById(id);
    }

    public void guardarControlAgua(ControlAgua controlAgua){
        controlAguaRepository.save(controlAgua);
    }

    public Integer cantidadMinutosAcumulados(LocalTime horaInicio, LocalTime horaFin){
        Duration duracion = Duration.between(horaInicio, horaFin);
        return (Integer) (int) duracion.toMinutes();
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

    public List<ControlAgua> findByUsuarioIdExport(Long id){
        return controlAguaRepository.findByUsuarioId(id);
    }
}
