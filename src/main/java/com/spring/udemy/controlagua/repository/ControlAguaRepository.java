package com.spring.udemy.controlagua.repository;

import com.spring.udemy.controlagua.model.ControlAgua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ControlAguaRepository extends JpaRepository<ControlAgua, Long> {
    Page<ControlAgua> findAll(Pageable pageable);
    Page<ControlAgua> findByUsuarioId(Long id, Pageable pageable);
    Page<ControlAgua> findByFechaRegistro(LocalDate fecha, Pageable pageable);

    List<ControlAgua> findByUsuarioId(Long id);

    @Query("SELECT DISTINCT YEAR(c.fechaRegistro) FROM ControlAgua c ORDER BY YEAR(c.fechaRegistro) DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT DISTINCT MONTH(c.fechaRegistro) FROM ControlAgua c WHERE YEAR(c.fechaRegistro) = :year ORDER BY MONTH(c.fechaRegistro)")
    List<Integer> findDistinctMonthsByYear(@Param("year") int year);

    @Query("SELECT DISTINCT DAY(c.fechaRegistro) FROM ControlAgua c WHERE YEAR(c.fechaRegistro) = :year AND MONTH(c.fechaRegistro) = :month ORDER BY DAY(c.fechaRegistro)")
    List<Integer> findDistinctDaysByYearAndMonth(@Param("year") int year, @Param("month") int month);

    Page<ControlAgua> findByFechaRegistroBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

}
