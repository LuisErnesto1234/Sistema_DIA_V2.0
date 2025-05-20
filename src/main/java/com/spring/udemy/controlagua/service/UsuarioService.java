package com.spring.udemy.controlagua.service;

import com.spring.udemy.controlagua.model.Rol;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolService rolService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolService = rolService;
        this.passwordEncoder = passwordEncoder;
    }

    public void guardarUsuario(Usuario usuario) {

//        if (usuario.getId() != null) {
//            // Es una actualización
//            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
//                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//            if (usuarioExistente != null && usuarioExistente.getFotoPerfil() != null && file.isEmpty()){
//                usuario.setFotoPerfil(usuarioExistente.getFotoPerfil());
//            } else if (!file.isEmpty()) {
//
//                String rutaBase = System.getProperty("user.dir");
//
//                String rutaSubida = rutaBase + File.separator + "uploads" + File.separator + "perfil";
//
//                File carpetaSubida = new File(rutaSubida);
//                if (!carpetaSubida.exists()) {
//                    boolean crearCarpeta = carpetaSubida.mkdirs();
//                    if (!crearCarpeta){
//                        throw new RuntimeException("No se pudo crear la carpeta de subida");
//                    }
//                }
//
//                String nombreImagen = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//                Path rutaImagen = Paths.get(rutaSubida + File.separator + nombreImagen);
//
//                file.transferTo(rutaImagen.toFile());
//
//                usuario.setFotoPerfil("/uploads/perfil/" + nombreImagen);
//            }
//
//            if (file.isEmpty() && usuario.getFotoPerfil() == null){
//                usuario.setRoles(Set.of(rolService.getRolByNombre("USER").orElseThrow()));
//                usuario.setFotoPerfil("/uploads/perfil/default.png");
//            }
//
//
//            if (!usuario.getContrasenia().equals(usuarioExistente.getContrasenia())) {
//                usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
//            }
//        } else {
//            // Es un nuevo usuario
//            usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
//        }

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

    public void agregarRolAdmin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rol rolAdmin = rolService.getRolByNombre("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

        usuario.getRoles().add(rolAdmin);
        usuarioRepository.save(usuario);
    }

    public void removerRolAdmin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rol rolAdmin = rolService.getRolByNombre("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

        usuario.getRoles().removeIf(r -> r.getNombre().equals("ADMIN"));
        usuarioRepository.save(usuario);
    }

    public void registrarUsuario(Usuario usuario) {
        usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));

        Rol rolUser = rolService.getRolByNombre("USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        usuario.setRoles(Set.of(rolUser));
        usuarioRepository.save(usuario);
    }
}
