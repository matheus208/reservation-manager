package com.volcano.reservationmanager.repositories.models;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, UUID> {

	@Query("select r from Reservation r where (r.startDate >= ?1 and r.startDate <= ?2) or (r.endDate >= ?1 and r.endDate <= ?2)")
	Stream<Reservation> findBetweenDate(LocalDate from, LocalDate to);

	Stream<Reservation> findByNameAndEmailAndEndDateAfter(String name, String email, LocalDate after);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Reservation save(Reservation reservation);

}
