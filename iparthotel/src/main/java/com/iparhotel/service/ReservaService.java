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

			throw new ReservaException(CodigoError.DATOS_INVALIDOS, "Las fechas no son validas");
		}
	}

	private Habitacion buscarHabitacion(Long id) {
		return habitacionRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Habitacion no encontrada"));
	}

	private Usuario buscarUsuario(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Usuario no encontrado"));
	}

	private Reserva buscarReserva(Long id) {
		return reservaRepository.findById(id)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Reserva no encontrada"));
	}

	private boolean hayConflicto(Long habitacionId, LocalDate entrada, LocalDate salida, Long excluirId) {

		return reservaRepository.contarSolapamientos(habitacionId, entrada, salida, EstadoReserva.CONFIRMADA,
				excluirId) > 0;
	}

	@Transactional(readOnly = true)
	public boolean consultarDisponibilidad(Long habitacionId, LocalDate entrada, LocalDate salida) {

		validarFechas(entrada, salida);
		buscarHabitacion(habitacionId);

		return !hayConflicto(habitacionId, entrada, salida, null);
	}

	@Transactional(readOnly = true)
	public List<Habitacion> habitacionesDisponibles(LocalDate entrada, LocalDate salida) {

		validarFechas(entrada, salida);

		return habitacionRepository.findAllByOrderByNumeroAsc().stream()
				.filter(h -> !hayConflicto(h.getId(), entrada, salida, null)).toList();
	}

	@Transactional
	public Reserva crearReserva(Long usuarioId, Long habitacionId, LocalDate entrada, LocalDate salida,
			boolean camaSupletoria, String peticiones) {

		validarFechas(entrada, salida);

		Habitacion habitacion = habitacionRepository.bloquearPorId(habitacionId)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Habitacion no encontrada"));

		Usuario usuario = buscarUsuario(usuarioId);

		if (hayConflicto(habitacionId, entrada, salida, null)) {

			throw new ReservaException(CodigoError.CONFLICTO, "Habitacion ocupada en esas fechas");
		}

		Reserva reserva = new Reserva(usuario, habitacion, entrada, salida, camaSupletoria, peticiones);

		return reservaRepository.save(reserva);
	}

	@Transactional(readOnly = true)
	public List<Reserva> listarReservasCliente(Long usuarioId) {

		return reservaRepository.findByUsuarioIdOrderByFechaEntradaDesc(usuarioId);
	}

	@Transactional
	public Reserva modificarReserva(Long reservaId, Long usuarioId, LocalDate entrada, LocalDate salida) {

		validarFechas(entrada, salida);

		Reserva reserva = buscarReserva(reservaId);

		if (!reserva.getUsuario().getId().equals(usuarioId)) {

			throw new ReservaException(CodigoError.ACCESO_DENEGADO, "No puedes modificar esta reserva");
		}

		Long habitacionId = reserva.getHabitacion().getId();

		habitacionRepository.bloquearPorId(habitacionId)
				.orElseThrow(() -> new ReservaException(CodigoError.NO_ENCONTRADO, "Habitacion no encontrada"));

		if (reserva.getEstado() == EstadoReserva.CANCELADA) {
			throw new ReservaException(CodigoError.CONFLICTO, "La reserva esta cancelada");
		}

		if (hayConflicto(habitacionId, entrada, salida, reservaId)) {

			throw new ReservaException(CodigoError.CONFLICTO, "Habitacion ocupada en las nuevas fechas");
		}

		reserva.setFechaEntrada(entrada);
		reserva.setFechaSalida(salida);
		reserva.setPrecioTotal(reserva.calcularTotal());

		return reservaRepository.save(reserva);
	}

	@Transactional
	public void cancelarReserva(Long reservaId, Long usuarioId) {

		Reserva reserva = buscarReserva(reservaId);

		if (!reserva.getUsuario().getId().equals(usuarioId)) {

			throw new ReservaException(CodigoError.ACCESO_DENEGADO, "No puedes cancelar esta reserva");
		}

		if (reserva.getEstado() == EstadoReserva.CANCELADA) {
			throw new ReservaException(CodigoError.CONFLICTO, "La reserva ya esta cancelada");
		}

		reserva.cancelar();
		reservaRepository.save(reserva);
	}

	@Transactional(readOnly = true)
	public List<Reserva> listarTodas() {
		return reservaRepository.findAll();
	}
}
