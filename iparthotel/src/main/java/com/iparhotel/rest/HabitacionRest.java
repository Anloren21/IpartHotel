
package com.iparhotel.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import com.iparhotel.entidades.Habitacion;
import com.iparhotel.excepciones.*;
import com.iparhotel.repositorios.HabitacionRepository;
import com.iparhotel.service.ReservaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/habitaciones")
@RequiredArgsConstructor
public class HabitacionRest {

	private final HabitacionRepository habitacionRepository;
	private final ReservaService reservaService;

	public record HabitacionDTO(Long id, String numero, String tipo, int capacidad, BigDecimal precioNoche) {
		public static HabitacionDTO desde(Habitacion h) {
			return new HabitacionDTO(h.getId(), h.getNumero(), h.getClass().getSimpleName(), h.getCapacidad(),
					h.getPrecioNoche());
		}
	}

	public record DisponibilidadDTO(Long habitacionId, boolean disponible) {
	}

	@GetMapping
	public List<HabitacionDTO> listar(@RequestParam(required = false) LocalDate entrada,
			@RequestParam(required = false) LocalDate salida) {

		if (entrada == null && salida == null) {
			return habitacionRepository.findAllByOrderByNumeroAsc().stream().map(HabitacionDTO::desde).toList();
		}

		return reservaService.habitacionesDisponibles(entrada, salida).stream().map(HabitacionDTO::desde).toList();
	}

	@GetMapping("/{id}")
	public HabitacionDTO detalle(@PathVariable Long id) {

		Habitacion habitacion = habitacionRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Habitacion no encontrada"));

		return HabitacionDTO.desde(habitacion);
	}

	@GetMapping("/{id}/disponibilidad")
	public DisponibilidadDTO disponibilidad(@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entrada,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida) {

		boolean disponible = reservaService.consultarDisponibilidad(id, entrada, salida);

		return new DisponibilidadDTO(id, disponible);
	}
}
