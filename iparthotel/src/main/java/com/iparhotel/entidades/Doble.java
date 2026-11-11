package com.iparhotel.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DOBLE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Doble extends Habitacion {

	private static final BigDecimal SUPLEMENTO_CAMA = new BigDecimal("20.00");

	public Doble(String numero, BigDecimal precioNoche) {
		super(numero, 2, precioNoche);
	}

	@Override
	public BigDecimal calcularPrecio(LocalDate entrada, LocalDate salida, boolean camaSupletoria) {

		long noches = calcularNoches(entrada, salida);

		BigDecimal precio = getPrecioNoche();

		if (camaSupletoria) {
			precio = precio.add(SUPLEMENTO_CAMA);
		}

		return precio.multiply(BigDecimal.valueOf(noches));
	}
}
