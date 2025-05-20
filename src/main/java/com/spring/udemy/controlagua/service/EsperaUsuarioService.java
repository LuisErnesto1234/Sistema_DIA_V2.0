package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.EsperaUsuarioControl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EsperaUsuarioService {

    private final List<EsperaUsuarioControl> esperaUsuarioList = new ArrayList<>();

    public void agregarUsuario(EsperaUsuarioControl esperaUsuarioControl) {
        esperaUsuarioControl.setNumeroOrden(esperaUsuarioList.size() + 1);

        if (esperaUsuarioList.stream().noneMatch(e -> "si".equals(e.getEstado()))) {
            // No hay nadie usando el grifo, este será el primero
            esperaUsuarioControl.setEstado("si");
            esperaUsuarioControl.setHoraInicio(LocalDateTime.now());
        } else {
            // Ya hay alguien usando, este se pone en espera
            esperaUsuarioControl.setEstado("no");
            esperaUsuarioControl.setHoraInicio(null);
        }

        esperaUsuarioList.add(esperaUsuarioControl);
    }

    public void iniciarTurno() {
        // 1. Eliminar al usuario que está usando el grifo (estado "si")
        Iterator<EsperaUsuarioControl> iterator = esperaUsuarioList.iterator();
        while (iterator.hasNext()) {
            EsperaUsuarioControl u = iterator.next();
            if ("si".equals(u.getEstado())) {
                u.setEstado("no"); // Por si lo vas a guardar en otro lado o registrar historial
                u.setHoraInicio(null);
                iterator.remove(); // Se elimina de la lista de espera
                break; // Solo puede haber uno con estado "si"
            }
        }

        // 2. Promover al siguiente en la fila, si hay
        Optional<EsperaUsuarioControl> siguiente = esperaUsuarioList.stream()
                .filter(u -> "no".equals(u.getEstado()))
                .min(Comparator.comparing(EsperaUsuarioControl::getNumeroOrden));

        siguiente.ifPresent(u -> {
            u.setEstado("si");
            u.setHoraInicio(LocalDateTime.now());
        });

        // 3. Reordenar número de orden para mantener la secuencia limpia
        for (int i = 0; i < esperaUsuarioList.size(); i++) {
            esperaUsuarioList.get(i).setNumeroOrden(i + 1);
        }
    }

    public EsperaUsuarioControl getUsuarioActual() {
        return esperaUsuarioList.stream()
                .filter(u -> "si".equals(u.getEstado()))
                .findFirst()
                .orElse(null);
    }

    public List<EsperaUsuarioControl> listaEsperaUsuario() {
        return esperaUsuarioList;
    }

    public void eliminarUsuario(EsperaUsuarioControl esperaUsuarioControl){
        esperaUsuarioList.remove(esperaUsuarioControl);
    }

    public EsperaUsuarioControl buscarUsuarioPorNumeroOrden(Integer numeroOrden){
        return esperaUsuarioList.stream()
                .filter(esperaUsuarioControl -> esperaUsuarioControl.getNumeroOrden().equals(numeroOrden))
                .findFirst()
                .orElse(null);
    }
}
