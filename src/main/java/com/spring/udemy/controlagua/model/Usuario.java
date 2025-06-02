package com.spring.udemy.controlagua.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres.")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 4, message = "El nombre debe tener al menos 4 caracteres.")
    private String apellido;
    @NotNull(message = "Los minutos acumulados son obligatorios")
    @Column(name = "minutos_acumulados")
    private Integer minutosAcumulados;
    @Column(name = "minutos_semana")
    private Integer minutosSemana;
    private String estado;
    private String correo;
    private String contrasenia;
    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ControlAgua> historial = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Usuario usuario = (Usuario) o;
        return getId() != null && Objects.equals(getId(), usuario.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public boolean tieneRol(String nombreRol) {
        return roles.stream().anyMatch(r -> r.getNombre().equals(nombreRol));
    }

    public String getNombreRolAmigable() {
        if (roles.stream().anyMatch(r -> r.getNombre().equals("ADMIN"))) {
            return "Administrador";
        } else if (roles.stream().anyMatch(r -> r.getNombre().equals("USER"))) {
            return "Usuario";
        }
        return "Desconocido";
    }

}
