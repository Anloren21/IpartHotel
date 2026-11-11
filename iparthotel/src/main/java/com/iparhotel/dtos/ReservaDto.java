
package com.iparhotel.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.iparhotel.entidades.EstadoReserva;
import com.iparhotel.entidades.Reserva;

public record ReservaDto(

		Long id, String habitacion, LocalDate fechaEntrada, LocalDate fechaSalida, boolean camaSupletoria,
		BigDecimal precioTotal, EstadoReserva estado, String peticionesEspeciales

) {

	public static ReservaDto desde(Reserva r) {

		return new ReservaDto(r.getId(), r.getHabitacion().getNumero(), r.getFechaEntrada(), r.getFechaSalida(),
				r.isCamaSupletoria(), r.getPrecioTotal(), r.getEstado(), r.getPeticionesEspeciales());
	}
}
