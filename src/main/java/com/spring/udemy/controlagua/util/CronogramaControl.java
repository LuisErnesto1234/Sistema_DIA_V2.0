package com.spring.udemy.controlagua.util;

import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CronogramaControl {

    private final UsuarioRepository usuarioRepository;
    private static final Logger logger = LoggerFactory.getLogger(CronogramaControl.class);

    public CronogramaControl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Scheduled(cron = "0 0 0 * * MON", zone = "America/Lima")
    @Transactional
    public void reiniciarMinutosSemanales() {
        try {
            logger.info("Tarea programada ejecutada: reiniciando minutos semanales...");
            List<Usuario> usuarioList = usuarioRepository.findAll();
            for (Usuario usuario : usuarioList) {
                Integer minutosSemanaSobrantes = usuario.getMinutosSemana();
                Integer minutosAcumulados = usuario.getMinutosAcumulados() != null ? usuario.getMinutosAcumulados() : 0;

                if (minutosSemanaSobrantes != null && minutosSemanaSobrantes > 0) {
                    usuario.setMinutosAcumulados(minutosSemanaSobrantes + minutosAcumulados);
                }

                usuario.setMinutosSemana(60); // Resetear semana
                usuarioRepository.save(usuario);
            }
        } catch (Exception e) {
            logger.error("Error al ejecutar tarea programada: ", e);
        }
    }

}
