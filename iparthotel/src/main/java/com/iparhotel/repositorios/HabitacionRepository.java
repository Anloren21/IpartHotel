
package com.iparhotel.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iparhotel.entidades.Habitacion;
import jakarta.persistence.LockModeType;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

	Optional<Habitacion> findByNumero(String numero);

	List<Habitacion> findAllByOrderByNumeroAsc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT h FROM Habitacion h WHERE h.id = :id")
	Optional<Habitacion> bloquearPorId(@Param("id") Long id);
}
