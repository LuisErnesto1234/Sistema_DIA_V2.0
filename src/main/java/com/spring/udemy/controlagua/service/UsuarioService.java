package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.Rol;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
            if (file.isEmpty()) {
                usuario.setFotoPerfil(
                        usuarioExistente.getFotoPerfil() != null ? usuarioExistente.getFotoPerfil() : "/uploads/perfil/default.png"
                );
            } else {
                usuario.setFotoPerfil(imagenService.guardarImagen(file));
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

        } else {
            // Nuevo usuario
            usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
            if (file.isEmpty()) {
                usuario.setFotoPerfil("/uploads/perfil/default.png");
            } else {
                usuario.setFotoPerfil(imagenService.guardarImagen(file));
            }
        }

        // ROL
        Rol rolUser = rolService.getRolByNombre("USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
        usuario.setEstado("no");
        usuario.setRoles(Set.of(rolUser));

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

    public void MinutosUtilizadosOperacion(Integer minutosUtilizados, Usuario usuario){
        if (usuario != null && minutosUtilizados != null){
            if (minutosUtilizados <= usuario.getMinutosSemana()){
                usuario.setMinutosSemana(usuario.getMinutosSemana() - minutosUtilizados);
            }else {
                if (usuario.getMinutosSemana() > 0){
                    Integer minutosRestantes = minutosUtilizados - usuario.getMinutosSemana();
                    usuario.setMinutosSemana(0);
                    usuario.setMinutosAcumulados(usuario.getMinutosAcumulados() - minutosRestantes);
                }else {
                    Integer minutosRestantes = usuario.getMinutosAcumulados() - minutosUtilizados;
                    usuario.setMinutosAcumulados(minutosRestantes);
                }
            }
        }
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
