package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.model.Rol;
import com.spring.udemy.controlagua.model.TipoRegistro;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;
    private final ImagenService imagenService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolService rolService, PasswordEncoder passwordEncoder, ImagenService imagenService) {
        this.usuarioRepository = usuarioRepository;
        this.rolService = rolService;
        this.passwordEncoder = passwordEncoder;
        this.imagenService = imagenService;
    }

    public void guardarUsuario(Usuario usuario, MultipartFile file) throws IOException {
        if (usuario.getId() != null) {
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // --- FOTO DE PERFIL ---
            if (file.isEmpty()){ //Imagen vacia {
                usuario.setFotoPerfil(
                        usuarioExistente.getFotoPerfil() != null ? usuarioExistente.getFotoPerfil() : "/uploads/perfil/default.png"
                );
            } else {//Imagen subida
                usuario.setFotoPerfil(imagenService.guardarImagen(file, usuarioExistente.getFotoPerfil()));
            }

            // --- CONTRASEÑA ---
            if (usuario.getContrasenia() == null || usuario.getContrasenia().isBlank()) {
                usuario.setContrasenia(usuarioExistente.getContrasenia()); // No se cambió
            } else {
                // Se ingresó algo nuevo: verificar si es diferente
                if (!passwordEncoder.matches(usuario.getContrasenia(), usuarioExistente.getContrasenia())) {
                    usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
                } else {
                    // Ya es igual (raro si está encriptada), pero por seguridad...
                    usuario.setContrasenia(usuarioExistente.getContrasenia());
                }
            }
            //En el caso de que sea una edición, mantenemos los datos del usuario actual
            usuario.setRoles(usuarioExistente.getRoles());
            usuario.setEstado(usuarioExistente.getEstado());

        } else {
            // Nuevo usuario
            usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
            if (file.isEmpty()) {
                usuario.setFotoPerfil("/uploads/perfil/default.png");
            } else {
                usuario.setFotoPerfil(imagenService.guardarImagen(file, null));
            }
            // ROL
            Rol rolUser = rolService.getRolByNombre("USER")
                    .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
            usuario.setRoles(Set.of(rolUser));
            usuario.setEstado("no");
        }

        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void eliminarUsuario(Long id){
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> listaUsuario(){
        return usuarioRepository.findAll();
    }

    public void MinutosUtilizadosOperacion(Integer minutosUtilizados, Usuario usuario, ControlAgua controlAgua) {
        if (usuario != null && minutosUtilizados != null) {

            int minutosSemana = usuario.getMinutosSemana() != null ? usuario.getMinutosSemana() : 0;
            int minutosAcumulados = usuario.getMinutosAcumulados() != null ? usuario.getMinutosAcumulados() : 0;

            if (minutosUtilizados <= minutosSemana) {
                usarMinutosDeSemana(usuario, minutosUtilizados);
                controlAgua.setTipoRegistro(TipoRegistro.USO);
            }
            else if (minutosUtilizados <= minutosSemana + minutosAcumulados) {
                int restantes = minutosUtilizados - minutosSemana;
                usarMinutosDeSemana(usuario, minutosSemana);
                usarMinutosAcumulados(usuario, restantes);
                controlAgua.setTipoRegistro(TipoRegistro.USO);
            }
            else {
                // No tiene suficientes minutos → RECARGA
                controlAgua.setTipoRegistro(TipoRegistro.RECARGA);
                controlAgua.setPrecio(minutosUtilizados * 0.5);
            }
        }
    }

    public void usarMinutosDeSemana(Usuario usuario, int minutos) {
        usuario.setMinutosSemana(usuario.getMinutosSemana() - minutos);
    }

    public void usarMinutosAcumulados(Usuario usuario, int minutosUtilizados) {
        Integer minutosRestantes = minutosUtilizados - usuario.getMinutosSemana();
        usuario.setMinutosSemana(0);
        usuario.setMinutosAcumulados(usuario.getMinutosAcumulados() - minutosRestantes);
    }

    public Page<Usuario> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Optional<Usuario> buscarUsuarioPorCorreo(String correo){
        return usuarioRepository.findByCorreo(correo);
    }

//    public void agregarRolAdmin(Long usuarioId) {
//        Usuario usuario = usuarioRepository.findById(usuarioId)
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        Rol rolAdmin = rolService.getRolByNombre("ADMIN")
//                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
//
//        usuario.getRoles().add(rolAdmin);
//        usuarioRepository.save(usuario);
//    }

//    public void removerRolAdmin(Long usuarioId) {
//        Usuario usuario = usuarioRepository.findById(usuarioId)
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        Rol rolAdmin = rolService.getRolByNombre("ADMIN")
//                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
//
//        usuario.getRoles().removeIf(r -> r.getNombre().equals("ADMIN"));
//        usuarioRepository.save(usuario);
//    }

//    public void registrarUsuario(Usuario usuario) {
//        usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
//
//        Rol rolUser = rolService.getRolByNombre("USER")
//                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
//
//        usuario.setRoles(Set.of(rolUser));
//        usuarioRepository.save(usuario);
//    }

}
