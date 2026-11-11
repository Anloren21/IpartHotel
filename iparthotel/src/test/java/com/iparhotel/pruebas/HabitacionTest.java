
package com.iparhotel.pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.iparhotel.entidades.*;
import com.iparhotel.excepciones.ReservaException;

class HabitacionTest {

	private final LocalDate entrada = LocalDate.of(2026, 12, 1);

	private final LocalDate salida = LocalDate.of(2026, 12, 7);

	@Test
	void probarPolimorfismo() {

		List<Habitacion> habitaciones = List.of(new Individual("101", new BigDecimal("60.00")),
				new Doble("201", new BigDecimal("100.00")), new Suite("301", new BigDecimal("150.00")));

		BigDecimal[] esperados = { new BigDecimal("360.00"), new BigDecimal("600.00"), new BigDecimal("855.00") };

		for (int i = 0; i < habitaciones.size(); i++) {

			Habitacion habitacion = habitaciones.get(i);

			BigDecimal total = habitacion.calcularPrecio(entrada, salida, false);

			System.out.println(habitacion.getClass().getSimpleName() + ": " + total + " EUR");

			assertEquals(0, esperados[i].compareTo(total));
		}
	}

	@Test
	void dobleConCamaSupletoria() {

		Habitacion doble = new Doble("202", new BigDecimal("100.00"));

		BigDecimal total = doble.calcularPrecio(entrada, salida, true);

		assertEquals(0, new BigDecimal("720.00").compareTo(total));
	}

	@Test
	void individualNoAdmiteCamaExtra() {

		Habitacion individual = new Individual("102", new BigDecimal("60.00"));

		assertThrows(ReservaException.class, () -> individual.calcularPrecio(entrada, salida, true));
	}

	@Test
	void fechasInvalidas() {

		Habitacion habitacion = new Suite("302", new BigDecimal("150.00"));

		assertThrows(ReservaException.class, () -> habitacion.calcularPrecio(salida, entrada, false));
	}

	@Test
	void suiteCincoNochesSinDescuento() {

		Habitacion suite = new Suite("303", new BigDecimal("150.00"));

		BigDecimal total = suite.calcularPrecio(entrada, entrada.plusDays(5), false);

		assertEquals(0, new BigDecimal("800.00").compareTo(total));
	}
}
