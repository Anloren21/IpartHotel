
package com.iparhotel.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.iparhotel.excepciones.ReservaException;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "habitacion_id", nullable = false)
	private Habitacion habitacion;

	@Column(nullable = false)
	private LocalDate fechaEntrada;

	@Column(nullable = false)
	private LocalDate fechaSalida;

	private boolean camaSupletoria;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precioTotal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoReserva estado;

	@Column(length = 1000)
	private String peticionesEspeciales;

	public Reserva(Usuario usuario, Habitacion habitacion, LocalDate fechaEntrada, LocalDate fechaSalida,
			boolean camaSupletoria, String peticionesEspeciales) {

		if (usuario == null || habitacion == null) {
			throw new ReservaException("El usuario y la habitación son obligatorios");
		}

		this.usuario = usuario;
		this.habitacion = habitacion;
		this.fechaEntrada = fechaEntrada;
		this.fechaSalida = fechaSalida;
		this.camaSupletoria = camaSupletoria;
		this.peticionesEspeciales = peticionesEspeciales;

		this.precioTotal = calcularTotal();
		this.estado = EstadoReserva.CONFIRMADA;
	}

	public BigDecimal calcularTotal() {
		return habitacion.calcularPrecio(fechaEntrada, fechaSalida, camaSupletoria);
	}

	public void cancelar() {
		if (estado == EstadoReserva.CANCELADA) {
			throw new ReservaException("La reserva ya está cancelada");
		}

		this.estado = EstadoReserva.CANCELADA;
	}
}
