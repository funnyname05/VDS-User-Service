package com.valledelsol.user;

import com.valledelsol.user.domain.Role;
import com.valledelsol.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User — pruebas unitarias")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Civil", "civil@test.com", "password123");
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor debe asignar rol CIVIL por defecto")
    void constructor_debeAsignarRolCivilPorDefecto() {
        assertThat(user.getRol()).isEqualTo(Role.CIVIL);
    }

    @Test
    @DisplayName("constructor debe asignar nombre, email y password correctamente")
    void constructor_debeAsignarCamposCorrectamente() {
        assertThat(user.getNombre()).isEqualTo("Civil");
        assertThat(user.getEmail()).isEqualTo("civil@test.com");
        assertThat(user.getPassword()).isEqualTo("password123");
    }

    // ── actualizarRol ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarRol debe cambiar el rol correctamente")
    void actualizarRol_debeCambiarRol() {
        user.actualizarRol(Role.ADMIN);
        assertThat(user.getRol()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("actualizarRol a FUNCIONARIO debe funcionar correctamente")
    void actualizarRol_aFuncionario_debeFuncionar() {
        user.actualizarRol(Role.FUNCIONARIO);
        assertThat(user.getRol()).isEqualTo(Role.FUNCIONARIO);
    }

    // ── UserDetails ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUsername debe retornar el email")
    void getUsername_debeRetornarEmail() {
        assertThat(user.getUsername()).isEqualTo("civil@test.com");
    }

    @Test
    @DisplayName("getAuthorities debe retornar ROLE_CIVIL por defecto")
    void getAuthorities_debeRetornarRolCivil() {
        var authorities = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();
        assertThat(authorities).contains("ROLE_CIVIL");
    }

    @Test
    @DisplayName("getAuthorities debe retornar ROLE_ADMIN después de actualizar rol")
    void getAuthorities_conRolAdmin_debeRetornarRoleAdmin() {
        user.actualizarRol(Role.ADMIN);
        var authorities = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();
        assertThat(authorities).contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("isAccountNonExpired debe retornar true")
    void isAccountNonExpired_debeRetornarTrue() {
        assertThat(user.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("isAccountNonLocked debe retornar true")
    void isAccountNonLocked_debeRetornarTrue() {
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("isCredentialsNonExpired debe retornar true")
    void isCredentialsNonExpired_debeRetornarTrue() {
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("isEnabled debe retornar true")
    void isEnabled_debeRetornarTrue() {
        assertThat(user.isEnabled()).isTrue();
    }

    // ── equals ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("dos usuarios con mismo ID deben ser iguales")
    void equals_conMismoId_debenSerIguales() {
        var otroUser = new User("Otro", "otro@test.com", "pass");
        ReflectionTestUtils.setField(otroUser, "id", 1L);
        assertThat(user).isEqualTo(otroUser);
    }

    @Test
    @DisplayName("dos usuarios con distinto ID deben ser diferentes")
    void equals_conDistintoId_debenSerDiferentes() {
        var otroUser = new User("Otro", "otro@test.com", "pass");
        ReflectionTestUtils.setField(otroUser, "id", 2L);
        assertThat(user).isNotEqualTo(otroUser);
    }
}
