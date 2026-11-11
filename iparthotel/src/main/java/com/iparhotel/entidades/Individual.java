package com.iparhotel.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.iparhotel.excepciones.ReservaException;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("INDIVIDUAL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Individual extends Habitacion {

	public Individual(String numero, BigDecimal precioNoche) {

		super(numero, 1, precioNoche);
	}

	@Override
	public BigDecimal calcularPrecio(LocalDate entrada, LocalDate salida, boolean camaSupletoria) {

		if (camaSupletoria) {
			throw new ReservaException("La habitación individual no admite cama supletoria");
		}

		long noches = calcularNoches(entrada, salida);

		return getPrecioNoche().multiply(BigDecimal.valueOf(noches));
	}

}
