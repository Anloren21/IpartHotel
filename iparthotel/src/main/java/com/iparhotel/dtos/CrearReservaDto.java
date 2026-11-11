
package com.iparhotel.dtos;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record CrearReservaDto(

		@NotNull Long habitacionId,

		@NotNull LocalDate fechaEntrada,

		@NotNull LocalDate fechaSalida,

		boolean camaSupletoria,

		String peticionesEspeciales

) {
}
