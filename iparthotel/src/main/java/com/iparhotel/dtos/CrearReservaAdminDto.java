package com.iparhotel.dtos;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record CrearReservaAdminDto(@NotNull Long clienteId, @NotNull Long habitacionId, @NotNull LocalDate fechaEntrada,
		@NotNull LocalDate fechaSalida, boolean camaSupletoria, String peticionesEspeciales) {
}
