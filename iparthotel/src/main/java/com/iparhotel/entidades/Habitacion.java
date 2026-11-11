package com.iparhotel.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.iparhotel.excepciones.ReservaException;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "habitacion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Habitacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false, unique = true)
	private String numero;

	@Min(1)
	@Column(nullable = false)
	private int capacidad;

	@NotNull
	@DecimalMin("0.01")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precioNoche;

	protected Habitacion(String numero, int capacidad, BigDecimal precioNoche) {

		this.numero = numero;
		this.capacidad = capacidad;
		this.precioNoche = precioNoche;
	}

	public long calcularNoches(LocalDate entrada, LocalDate salida) {

		if (entrada == null || salida == null || !salida.isAfter(entrada)) {

			throw new ReservaException("Las fechas de la reserva no son válidas");
		}

		return ChronoUnit.DAYS.between(entrada, salida);
	}

	public abstract BigDecimal calcularPrecio(LocalDate entrada, LocalDate salida, boolean camaSupletoria);
}
