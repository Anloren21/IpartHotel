
package com.iparhotel.configuracion;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iparhotel.entidades.*;
import com.iparhotel.repositorios.*;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

	private final UsuarioRepository usuarioRepository;
	private final HabitacionRepository habitacionRepository;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		cargarHabitaciones();
		cargarUsuarios();

		System.out.println("=== DATOS INICIALES IPARHOTEL CARGADOS ===");
	}

	private void cargarHabitaciones() {

		List<Habitacion> habitaciones = List.of(

				new Individual("101", new BigDecimal("60.00")), new Individual("102", new BigDecimal("65.00")),
				new Individual("103", new BigDecimal("70.00")), new Individual("104", new BigDecimal("75.00")),
				new Individual("105", new BigDecimal("80.00")),

				new Doble("201", new BigDecimal("100.00")), new Doble("202", new BigDecimal("110.00")),
				new Doble("203", new BigDecimal("120.00")), new Doble("204", new BigDecimal("130.00")),
				new Doble("205", new BigDecimal("140.00")),

				new Suite("301", new BigDecimal("150.00")), new Suite("302", new BigDecimal("170.00")),
				new Suite("303", new BigDecimal("190.00")), new Suite("304", new BigDecimal("210.00")),
				new Suite("305", new BigDecimal("250.00")));

		for (Habitacion habitacion : habitaciones) {

			if (habitacionRepository.findByNumero(habitacion.getNumero()).isEmpty()) {

				habitacionRepository.save(habitacion);
			}
		}
	}

	private void cargarUsuarios() {

		crearUsuario("Administrador Hotel", "admin@iparhotel.test", "IPAR_ADMIN_PASSWORD", Rol.ADMIN);

		crearUsuario("Laura Garcia", "laura@iparhotel.test", "IPAR_LAURA_PASSWORD", Rol.CLIENTE);

		crearUsuario("Carlos Martinez", "carlos@iparhotel.test", "IPAR_CARLOS_PASSWORD", Rol.CLIENTE);
	}

	private void crearUsuario(String nombre, String email, String variablePassword, Rol rol) {

		if (usuarioRepository.findByEmail(email).isPresent()) {
			return;
		}

		String password = environment.getRequiredProperty(variablePassword);

		if (password.isBlank()) {
			throw new IllegalStateException("Contraseña vacía: " + variablePassword);
		}

		Usuario usuario = new Usuario();

		usuario.setNombre(nombre);
		usuario.setEmail(email);

		usuario.setPassword(passwordEncoder.encode(password));

		usuario.setRol(rol);

		usuarioRepository.save(usuario);
	}
}
