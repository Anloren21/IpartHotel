
package com.iparhotel.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iparhotel.entidades.Reserva;
import com.iparhotel.entidades.EstadoReserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	List<Reserva> findByUsuarioIdOrderByFechaEntradaDesc(Long usuarioId);

	List<Reserva> findByEstado(EstadoReserva estado);

	@Query("""
			SELECT COUNT(r)
			FROM Reserva r
			WHERE r.habitacion.id = :habitacionId
			AND r.estado = :estado
			AND r.fechaEntrada < :salida
			AND r.fechaSalida > :entrada
			AND (:excluirId IS NULL OR r.id <> :excluirId)
			""")
	long contarSolapamientos(@Param("habitacionId") Long habitacionId, @Param("entrada") LocalDate entrada,
			@Param("salida") LocalDate salida, @Param("estado") EstadoReserva estado,
			@Param("excluirId") Long excluirId);
}
