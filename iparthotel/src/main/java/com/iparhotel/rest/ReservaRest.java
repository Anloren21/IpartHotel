package com.iparhotel.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.iparhotel.dtos.*;
import com.iparhotel.entidades.Usuario;
import com.iparhotel.excepciones.*;
import com.iparhotel.repositorios.UsuarioRepository;
import com.iparhotel.service.ReservaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaRest{

	private final ReservaService reservaService;
	private final UsuarioRepository usuarioRepository;

	private Long obtenerUsuarioId(Jwt jwt) {

		Usuario usuario = usuarioRepository.findByEmail(jwt.getSubject())
				.orElseThrow(() -> new ReservaException(CodigoError.ACCESO_DENEGADO, "Usuario no autorizado"));

		return usuario.getId();
	}

	@PostMapping
	public ResponseEntity<ReservaDto> crear(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CrearReservaDto datos) {

		var reserva = reservaService.crearReserva(obtenerUsuarioId(jwt), datos.habitacionId(), datos.fechaEntrada(),
				datos.fechaSalida(), datos.camaSupletoria(), datos.peticionesEspeciales());

		return ResponseEntity.status(HttpStatus.CREATED).body(ReservaDto.desde(reserva));
	}

	@GetMapping("/mis-reservas")
	public List<ReservaDto> misReservas(@AuthenticationPrincipal Jwt jwt) {

		return reservaService.listarReservasCliente(obtenerUsuarioId(jwt)).stream().map(ReservaDto::desde)
				.toList();
	}

	@PutMapping("/{id}")
	public ReservaDto modificar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody ModificarReservaDto datos) {

		return ReservaDto.desde(
				reservaService.modificarReserva(id, obtenerUsuarioId(jwt), datos.fechaEntrada(), datos.fechaSalida()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

		reservaService.cancelarReserva(id, obtenerUsuarioId(jwt));

		return ResponseEntity.noContent().build();
	}
}
