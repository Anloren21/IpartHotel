package com.iparhotel.dtos;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record ModificarReservaDto(

		@NotNull LocalDate fechaEntrada,

		@NotNull LocalDate fechaSalida

) {
}
