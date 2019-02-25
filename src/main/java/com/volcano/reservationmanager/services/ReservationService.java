package com.volcano.reservationmanager.services;

import com.volcano.reservationmanager.converters.ReservationConverter;
import com.volcano.reservationmanager.exceptions.InvalidReservationException;
import com.volcano.reservationmanager.exceptions.NotFoundException;
import com.volcano.reservationmanager.models.ReservationDTO;
import com.volcano.reservationmanager.repositories.models.Reservation;
import com.volcano.reservationmanager.repositories.models.ReservationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService {

	private final ReservationRepository repository;
	private final ReservationConverter reservationConverter;

	public ReservationService(ReservationRepository repository,
							  ReservationConverter reservationConverter) {
		this.repository = repository;
		this.reservationConverter = reservationConverter;
	}

	@Transactional
	public List<ReservationDTO> findReservations(final LocalDate from, final LocalDate to) {
		return repository
				.findBetweenDate(from, to)
				.map(reservationConverter::toDTO)
				.collect(Collectors.toList());
	}

	@Transactional
	@Cacheable("reservation")
	public ReservationDTO findReservation(UUID reservationId) {
		return repository
				.findById(reservationId)
				.map(reservationConverter::toDTO)
				.orElseThrow(() -> new NotFoundException(String.format("Reservation %s not found", reservationId)));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
	@CachePut(value = "reservation", key = "#result.id")
	public ReservationDTO createReservation(final ReservationDTO reservation) {
		validate(reservation);
		Reservation savedReservation = repository.save(reservationConverter.toModel(reservation));
		return reservationConverter.toDTO(savedReservation);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
	@CachePut(value = "reservation", key = "#reservation.id")
	public ReservationDTO editReservation(ReservationDTO reservation) {

		Optional<Reservation> optionalSavedReservation = repository.findById(reservation.getId());
		if (!optionalSavedReservation.isPresent()) {
			throw new NotFoundException(String.format("Reservation %s not found", reservation.getId()));
		}

		Reservation savedReservation = optionalSavedReservation.get();

		validate(reservation);
		savedReservation.setStartDate(reservation.getFrom());
		savedReservation.setEndDate(reservation.getTo());

		return reservationConverter.toDTO(savedReservation);
	}

	@CacheEvict("reservation")
	public void cancelReservation(UUID reservationId) {
		repository.deleteById(reservationId);
	}

	private void validate(final ReservationDTO reservation) {

		// At most 30 days
		if (DAYS.between(LocalDate.now(), reservation.getFrom()) > 30) {
			throw new InvalidReservationException("Reservation too far ahead. Reservations can be made up to 30 days in advance");
		}

		long daysBetween = DAYS.between(reservation.getFrom(), reservation.getTo());

		// Start date after End date
		if (daysBetween < 0) {
			throw new InvalidReservationException(
					"Reservation has invalid range, make sure the start date is before the end date.");
		}

		//Cannot last more than 3 days
		if (0 >= daysBetween || daysBetween > 3) {
			throw new InvalidReservationException(
					String.format("Reservation lasts %d days. Must be between 1 and 3 days.", daysBetween));
		}

		//Client already has a valid booking
		repository.findByNameAndEmailAndEndDateAfter(reservation.getName(), reservation.getEmail(), LocalDate.now())
				.filter(found -> !found.getId().equals(reservation.getId()))
				.findFirst()
				.ifPresent(r -> {
					throw new InvalidReservationException(String.format(
						"There is already a valid reservation in place for you. Reservation id: %s",
						r.getId().toString()));
				});

		//There is a valid booking for those dates
		repository.findBetweenDate(reservation.getFrom(), reservation.getTo())
				.findFirst()
				.filter(found -> !found.getId().equals(reservation.getId()))
				.ifPresent(r -> {
					throw new InvalidReservationException("There is a valid reservation in place for those dates.");
				});

	}

	public ReservationDTO findReservationById(UUID reservationId) {
		return repository.findById(reservationId)
				.map(reservationConverter::toDTO)
				.orElseThrow(() -> new NotFoundException(String.format("Reservation id %s not found", reservationId)));
	}

}
