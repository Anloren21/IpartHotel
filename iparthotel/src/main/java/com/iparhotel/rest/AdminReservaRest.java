
package com.iparhotel.rest;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.iparhotel.dtos.*;
import com.iparhotel.service.ReservaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/reservas")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReservaRest {

	private final ReservaService reservaService;

	@GetMapping
	public List<ReservaDto> listar() {

		return reservaService.listarTodasReservas().stream().map(ReservaDto::desde).toList();
	}

	@PostMapping
	public ResponseEntity<ReservaDto> crear(@Valid @RequestBody CrearReservaAdminDto datos) {

		var reserva = reservaService.crearReservaAdministrativa(datos.clienteId(), datos.habitacionId(),
				datos.fechaEntrada(), datos.fechaSalida(), datos.camaSupletoria(), datos.peticionesEspeciales());

		return ResponseEntity.status(HttpStatus.CREATED).body(ReservaDto.desde(reserva));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> cancelar(@PathVariable Long id) {

		reservaService.cancelarReservaAdministrativa(id);

		return ResponseEntity.noContent().build();
	}
}
