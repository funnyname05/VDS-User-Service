package com.valledelsol.user.controller;

import com.valledelsol.user.domain.ActualizarRolDTO;
import com.valledelsol.user.domain.User;
import com.valledelsol.user.domain.UserRepository;
import com.valledelsol.user.domain.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // Listar todos los usuarios — solo ADMIN vía BFF
    @GetMapping("/usuarios")
    public ResponseEntity<List<UserResponseDTO>> listarUsuarios() {
        var usuarios = userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    // Cambiar rol de un usuario
    @PutMapping("/admin/usuarios/{id}/rol")
    public ResponseEntity<UserResponseDTO> cambiarRol(
            @PathVariable Long id,
            @RequestBody @Valid ActualizarRolDTO datos) {

        Optional<User> usuarioOptional = userRepository.findById(id);
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User usuario = usuarioOptional.get();
        usuario.actualizarRol(datos.nuevoRol());
        userRepository.save(usuario);
        return ResponseEntity.ok(new UserResponseDTO(usuario));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UserResponseDTO> obtenerUsuario(@PathVariable Long id) {
        Optional<User> usuario = userRepository.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new UserResponseDTO(usuario.get()));
    }

    @PostMapping("/usuarios/registro")
    public ResponseEntity<String> registro(@RequestBody RegistroRequest datos) {
        if (userRepository.findByEmail(datos.email()) != null) {
            return ResponseEntity.badRequest().body("Email ya registrado");
        }
        var user = new User(
                datos.nombre(),
                datos.email(),
                datos.password() // ya viene encriptado desde auth-service
        );
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Perfil creado");
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record RegistroRequest(String nombre, String email, String password) {}
}
