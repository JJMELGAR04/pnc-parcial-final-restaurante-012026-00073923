package com.uca.pncparcialfinalrestaurante.security;

import com.uca.pncparcialfinalrestaurante.domain.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Id y sucursalId planos, para no tener que castear a CustomUserDetails
     * en todos lados donde se necesite el usuario autenticado.
     */
    public Long getId() {
        return usuario.getId();
    }

    public Long getSucursalId() {
        return usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security espera el prefijo "ROLE_" para que hasRole("ADMINISTRADOR") funcione.
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}