
package com.iparhotel.pruebas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iparhotel.entidades.*;
import com.iparhotel.excepciones.*;
import com.iparhotel.repositorios.*;
import com.iparhotel.service.ReservaService;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

	@Mock
	private ReservaRepository reservaRepository;

	@Mock
	private HabitacionRepository habitacionRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private ReservaService reservaService;

	private Habitacion habitacion;
	private LocalDate entrada;
	private LocalDate salida;

	@BeforeEach
	void prepararDatos() {
		habitacion = new Doble("201", new BigDecimal("100.00"));

		entrada = LocalDate.now().plusDays(5);
		salida = entrada.plusDays(3);
	}

	@Test
	void habitacionDisponible() {

		when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));

		when(reservaRepository.contarSolapamientos(1L, entrada, salida, EstadoReserva.CONFIRMADA, null)).thenReturn(0L);

		assertTrue(reservaService.consultarDisponibilidad(1L, entrada, salida));
	}

	@Test
	void habitacionOcupada() {

		when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));

		when(reservaRepository.contarSolapamientos(1L, entrada, salida, EstadoReserva.CONFIRMADA, null)).thenReturn(1L);

		assertFalse(reservaService.consultarDisponibilidad(1L, entrada, salida));
	}

	@Test
	void habitacionInexistente() {

		when(habitacionRepository.findById(999L)).thenReturn(Optional.empty());

		ReservaException ex = assertThrows(ReservaException.class,
				() -> reservaService.consultarDisponibilidad(999L, entrada, salida));

		assertEquals(CodigoError.NO_ENCONTRADO, ex.getCodigo());
	}

	@Test
	void fechasIncorrectas() {

		ReservaException ex = assertThrows(ReservaException.class,
				() -> reservaService.consultarDisponibilidad(1L, salida, entrada));

		assertEquals(CodigoError.DATOS_INVALIDOS, ex.getCodigo());
	}
}
