package com.valledelsol.user;

import com.valledelsol.user.controller.AdminController;
import com.valledelsol.user.domain.ActualizarRolDTO;
import com.valledelsol.user.domain.Role;
import com.valledelsol.user.domain.User;
import com.valledelsol.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController — pruebas unitarias")
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminController adminController;

    private User admin;
    private User civil;
    private User funcionario;

    @BeforeEach
    void setUp() {
        admin = new User("Admin", "admin@test.com", "encodedPass");
        admin.actualizarRol(Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);

        civil = new User("Civil", "civil@test.com", "encodedPass");
        ReflectionTestUtils.setField(civil, "id", 2L);

        funcionario = new User("Funcionario", "funcionario@test.com", "encodedPass");
        funcionario.actualizarRol(Role.FUNCIONARIO);
        ReflectionTestUtils.setField(funcionario, "id", 3L);
    }

    // ── listarUsuarios ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarUsuarios debe retornar 200 con lista de usuarios")
    void listarUsuarios_debeRetornar200ConLista() {
        when(userRepository.findAll()).thenReturn(List.of(admin, civil, funcionario));

        var response = adminController.listarUsuarios();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
    }

    @Test
    @DisplayName("listarUsuarios con BD vacía debe retornar lista vacía")
    void listarUsuarios_conBDVacia_debeRetornarListaVacia() {
        when(userRepository.findAll()).thenReturn(List.of());

        var response = adminController.listarUsuarios();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("listarUsuarios debe llamar al repositorio exactamente una vez")
    void listarUsuarios_debeLlamarRepositorioUnaVez() {
        when(userRepository.findAll()).thenReturn(List.of(admin));

        adminController.listarUsuarios();

        verify(userRepository, times(1)).findAll();
    }

    // ── obtenerUsuario ────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerUsuario con ID existente debe retornar 200")
    void obtenerUsuario_conIdExistente_debeRetornar200() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        var response = adminController.obtenerUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("obtenerUsuario con ID inexistente debe retornar 404")
    void obtenerUsuario_conIdInexistente_debeRetornar404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        var response = adminController.obtenerUsuario(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── cambiarRol ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cambiarRol con ID existente debe retornar 200 con rol actualizado")
    void cambiarRol_conIdExistente_debeRetornar200() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(civil));
        when(userRepository.save(any(User.class))).thenReturn(civil);

        var datos = new ActualizarRolDTO(Role.FUNCIONARIO);
        var response = adminController.cambiarRol(2L, datos);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(userRepository, times(1)).save(civil);
    }

    @Test
    @DisplayName("cambiarRol con ID inexistente debe retornar 404")
    void cambiarRol_conIdInexistente_debeRetornar404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        var datos = new ActualizarRolDTO(Role.ADMIN);
        var response = adminController.cambiarRol(99L, datos);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarRol no debe guardar si el usuario no existe")
    void cambiarRol_conIdInexistente_noDebeGuardar() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        adminController.cambiarRol(99L, new ActualizarRolDTO(Role.ADMIN));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("cambiarRol debe actualizar el rol correctamente")
    void cambiarRol_debeActualizarRolCorrectamente() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(civil));
        when(userRepository.save(any(User.class))).thenReturn(civil);

        adminController.cambiarRol(2L, new ActualizarRolDTO(Role.ADMIN));

        assertThat(civil.getRol()).isEqualTo(Role.ADMIN);
    }

    // ── registro ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registro con email nuevo debe retornar 201")
    void registro_conEmailNuevo_debeRetornar201() {
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(civil);

        var request = new AdminController.RegistroRequest("Nuevo", "nuevo@test.com", "encodedPass");
        var response = adminController.registro(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Perfil creado");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("registro con email existente debe retornar 400")
    void registro_conEmailExistente_debeRetornar400() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(admin);

        var request = new AdminController.RegistroRequest("Admin", "admin@test.com", "pass");
        var response = adminController.registro(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Email ya registrado");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registro debe asignar rol CIVIL por defecto")
    void registro_debeAsignarRolCivilPorDefecto() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        var request = new AdminController.RegistroRequest("Nuevo", "nuevo@test.com", "pass");
        adminController.registro(request);

        verify(userRepository).save(argThat(user ->
                user.getRol() == Role.CIVIL
        ));
    }
}
