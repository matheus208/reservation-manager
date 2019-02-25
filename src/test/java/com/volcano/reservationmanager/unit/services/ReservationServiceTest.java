package com.volcano.reservationmanager.unit.services;


import com.volcano.reservationmanager.converters.ReservationConverter;
import com.volcano.reservationmanager.exceptions.InvalidReservationException;
import com.volcano.reservationmanager.models.ReservationDTO;
import com.volcano.reservationmanager.repositories.models.Reservation;
import com.volcano.reservationmanager.repositories.models.ReservationRepository;
import com.volcano.reservationmanager.services.ReservationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

	@InjectMocks
	private ReservationService subject;

	@Mock
	private ReservationRepository repository;

	@Mock
	private ReservationConverter converter;


	@Test
	public void validates_reservation_dates_valid_range() {
		//Given
		ReservationDTO wrongReservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10), // from after
				LocalDate.of(2019, 1, 1)   // to
		);

		//Then/When
		Assertions.assertThrows(
				InvalidReservationException.class,
				() -> subject.createReservation(wrongReservation)
		);

	}

	@Test
	public void validates_reservation_dates_same_day() {
		//Given
		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),  // same date not allowed
				LocalDate.of(2019, 1, 10)   // (check-in & check-out time is @ 12:00)
		);

		//Then/When
		Assertions.assertThrows(InvalidReservationException.class, () -> subject.createReservation(reservation));

	}

	@Test
	public void validates_reservation_dates_correct_date() {
		//Given
		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		);

		when(converter.toModel(reservation)).thenReturn(new Reservation(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		));

		when(repository.save(any())).thenReturn(new Reservation(
				UUID.randomUUID(),
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		));

		when(repository.findByNameAndEmailAndEndDateAfter(any(), any(), any()))
				.thenReturn(Stream.empty());

		//Then/When
		Assertions.assertDoesNotThrow(() -> subject.createReservation(reservation));

	}

	@Test
	public void validates_reservation_already_exists() {
		//Given

		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		);

		when(repository.findByNameAndEmailAndEndDateAfter(any(), any(), any()))
				.thenReturn(Stream.of(new Reservation(UUID.randomUUID(), "", "", LocalDate.now(), LocalDate.now())));

		//When/Then
		Assertions.assertThrows(InvalidReservationException.class, () -> subject.createReservation(reservation));

	}

	@Test
	public void validates_reservation_too_far() {
		//Given

		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(2, ChronoUnit.MONTHS),
				LocalDate.now().plus(2, ChronoUnit.MONTHS).plus(1, ChronoUnit.DAYS)
		);

		//When/Then
		Assertions.assertThrows(InvalidReservationException.class, () -> subject.createReservation(reservation));

	}

	@Test
	public void returns_valid_uuid() {
		//Given
		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		);

		Reservation savedReservation = new Reservation(
				UUID.randomUUID(),
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		);

		when(converter.toModel(reservation)).thenReturn(new Reservation(
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		));

		when(converter.toDTO(savedReservation)).thenReturn(new ReservationDTO(
				UUID.randomUUID(),
				"Matheus",
				"matheus@email.com",
				LocalDate.of(2019, 1, 10),
				LocalDate.of(2019, 1, 12)
		));


		when(repository.save(any())).thenReturn(savedReservation);

		when(repository.findByNameAndEmailAndEndDateAfter(any(), any(), any()))
				.thenReturn(Stream.empty());

		//When
		ReservationDTO created = subject.createReservation(reservation);

		//Then
		Assertions.assertNotNull(created);
		Assertions.assertNotNull(created.getId());

	}


}