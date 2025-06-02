package com.spring.udemy.controlagua.repository;

import com.spring.udemy.controlagua.model.HistorialControles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialControlAguaRepository extends JpaRepository<HistorialControles, Long> {
    Page<HistorialControles> findAll(Pageable pageable);
}
