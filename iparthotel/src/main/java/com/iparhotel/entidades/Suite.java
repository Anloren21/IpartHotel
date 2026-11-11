package com.iparhotel.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.RoundingMode;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SUITE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suite extends Habitacion {

	private static final BigDecimal SUPLEMENTO_CAMA = new BigDecimal("20.00");

	private static final BigDecimal BIENVENIDA = new BigDecimal("50.00");

	private static final BigDecimal FACTOR_DESCUENTO = new BigDecimal("0.90");

	private final boolean jacuzzi = true;

	public Suite(String numero, BigDecimal precioNoche) {
		super(numero, 2, precioNoche);
	}

	@Override
	public BigDecimal calcularPrecio(LocalDate entrada, LocalDate salida, boolean camaSupletoria) {

		long noches = calcularNoches(entrada, salida);

		BigDecimal precio = getPrecioNoche();

		if (camaSupletoria) {
			precio = precio.add(SUPLEMENTO_CAMA);
		}

		BigDecimal total = precio.multiply(BigDecimal.valueOf(noches)).add(BIENVENIDA);

		if (noches > 5) {
			total = total.multiply(FACTOR_DESCUENTO);
		}

		return total.setScale(2, RoundingMode.HALF_UP);
	}
}
