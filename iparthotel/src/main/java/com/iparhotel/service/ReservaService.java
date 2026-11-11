package com.iparhotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iparhotel.entidades.*;
import com.iparhotel.excepciones.*;
import com.iparhotel.repositorios.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {

	private final ReservaRepository reservaRepository;
	private final HabitacionRepository habitacionRepository;
	private final UsuarioRepository usuarioRepository;

	private void validarFechas(LocalDate entrada, LocalDate salida) {

		if (entrada == null || salida == null || !salida.isAfter(entrada) || entrada.isBefore(LocalDate.now())) {

			throw new ReservaException(CodigoError.DATOS_INVALIDOS, "Fechas de reserva invalidas");
		}
	}

	private Habitacion obtenerHabitacion(Long id) {
		return habitacionRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Habitacion no encontrada"));
	}

	private Habitacion bloquearHabitacion(Long id) {
		return habitacionRepository.bloquearPorId(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Habitacion no encontrada"));
	}

	private Reserva obtenerReserva(Long id) {
		return reservaRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Reserva no encontrada"));
	}

	private Usuario obtenerUsuario(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Usuario no encontrado"));
	}

	private boolean existeConflicto(Long habitacionId, LocalDate entrada, LocalDate salida, Long excluirReservaId) {

		return reservaRepository.contarSolapamientos(habitacionId, entrada, salida, EstadoReserva.CONFIRMADA,
				excluirReservaId) > 0;
	}

	private void verificarPropietario(Reserva reserva, Long usuarioId) {

		if (!reserva.getUsuario().getId().equals(usuarioId)) {
			throw new ReservaException(CodigoError.ACCESO_DENEGADO, "No tienes permisos sobre esta reserva");
		}
	}

	// HU01 - CONSULTAR DISPONIBILIDAD

	@Transactional(readOnly = true)
	public boolean consultarDisponibilidad(Long habitacionId, LocalDate entrada, LocalDate salida) {

		validarFechas(entrada, salida);
		obtenerHabitacion(habitacionId);

		return !existeConflicto(habitacionId, entrada, salida, null);
	}

	@Transactional(readOnly = true)
	public List<Habitacion> habitacionesDisponibles(LocalDate entrada, LocalDate salida) {

		validarFechas(entrada, salida);

		return habitacionRepository.findAllByOrderByNumeroAsc().stream()
				.filter(h -> !existeConflicto(h.getId(), entrada, salida, null)).toList();
	}

	@Transactional
	public Reserva crearReserva(Long usuarioId, Long habitacionId, LocalDate entrada, LocalDate salida,
			boolean camaSupletoria, String peticiones) {

		validarFechas(entrada, salida);

		Habitacion habitacion = bloquearHabitacion(habitacionId);

		Usuario usuario = obtenerUsuario(usuarioId);

		if (existeConflicto(habitacionId, entrada, salida, null)) {

			throw new ReservaException(CodigoError.CONFLICTO, "La habitacion esta ocupada en esas fechas");
		}

		Reserva reserva = new Reserva(usuario, habitacion, entrada, salida, camaSupletoria, peticiones);

		return reservaRepository.saveAndFlush(reserva);
	}

	// HU05 - MIS RESERVAS

	@Transactional(readOnly = true)
	public List<Reserva> listarReservasCliente(Long usuarioId) {

		obtenerUsuario(usuarioId);

		return reservaRepository.findByUsuarioIdOrderByFechaEntradaDesc(usuarioId);
	}

	@Transactional
	public Reserva modificarReserva(Long reservaId, Long usuarioId, LocalDate entrada, LocalDate salida) {

		validarFechas(entrada, salida);

		Reserva reserva = obtenerReserva(reservaId);

		verificarPropietario(reserva, usuarioId);

		Long habitacionId = reserva.getHabitacion().getId();

		bloquearHabitacion(habitacionId);

		// Leer de nuevo tras obtener el bloqueo
		reserva = obtenerReserva(reservaId);

		verificarPropietario(reserva, usuarioId);

		if (reserva.getEstado() == EstadoReserva.CANCELADA) {
			throw new ReservaException(CodigoError.CONFLICTO, "No se puede modificar una reserva cancelada");
		}

		if (existeConflicto(habitacionId, entrada, salida, reservaId)) {

			throw new ReservaException(CodigoError.CONFLICTO, "No hay disponibilidad en las nuevas fechas");
		}

		reserva.setFechaEntrada(entrada);
		reserva.setFechaSalida(salida);
		reserva.setPrecioTotal(reserva.calcularTotal());

		return reservaRepository.saveAndFlush(reserva);
	}

	// HU06 - CANCELAR RESERVA

	@Transactional
	public void cancelarReserva(Long reservaId, Long usuarioId) {

		Reserva reserva = obtenerReserva(reservaId);

		verificarPropietario(reserva, usuarioId);

		Long habitacionId = reserva.getHabitacion().getId();

		bloquearHabitacion(habitacionId);

		reserva = obtenerReserva(reservaId);

		verificarPropietario(reserva, usuarioId);

		if (reserva.getEstado() == EstadoReserva.CANCELADA) {
			throw new ReservaException(CodigoError.CONFLICTO, "La reserva ya esta cancelada");
		}

		reserva.cancelar();
		reservaRepository.saveAndFlush(reserva);
	}

	@Transactional(readOnly = true)
	public List<Reserva> listarTodasReservas() {
		return reservaRepository.findAll();
	}

	@Transactional
	public Reserva crearReservaAdministrativa(Long clienteId, Long habitacionId, LocalDate entrada, LocalDate salida,
			boolean camaSupletoria, String peticiones) {

		return crearReserva(clienteId, habitacionId, entrada, salida, camaSupletoria, peticiones);
	}

	@Transactional
	public void cancelarReservaAdministrativa(Long reservaId) {

		Reserva reserva = obtenerReserva(reservaId);

		bloquearHabitacion(reserva.getHabitacion().getId());

		if (reserva.getEstado() == EstadoReserva.CANCELADA) {
			throw new ReservaException(CodigoError.CONFLICTO, "La reserva ya esta cancelada");
		}

		reserva.cancelar();
		reservaRepository.saveAndFlush(reserva);
	}

}
