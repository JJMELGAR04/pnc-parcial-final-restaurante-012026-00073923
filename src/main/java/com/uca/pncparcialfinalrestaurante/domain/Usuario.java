package com.uca.pncparcialfinalrestaurante.domain;

import com.uca.pncparcialfinalrestaurante.domain.enums.RolNombre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolNombre rol;

    /**
     * Solo aplica cuando rol = ENCARGADO_TURNO.
     * Es la sucursal a la que está atado: define qué mesas/pedidos puede gestionar.
     * Para ADMINISTRADOR queda en null (acceso total).
     * Para CLIENTE queda en null (no aplica, sus pedidos se filtran por su propio id, no por sucursal).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Column(nullable = false)
    private boolean activo = true;
}