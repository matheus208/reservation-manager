package com.volcano.reservationmanager.controllers;

import com.volcano.reservationmanager.exceptions.InvalidReservationException;
import com.volcano.reservationmanager.models.ReservationDTO;
import com.volcano.reservationmanager.services.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

	private final ReservationService service;

	public ReservationController(ReservationService service) {

		this.service = service;
	}

	@GetMapping("/{reservationId}")
	public ReservationDTO getReservation(@PathVariable UUID reservationId) {
		return service.findReservationById(reservationId);
	}

	@GetMapping
	public List<ReservationDTO> getReservations(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return service.findReservations(from, to);
	}

	@PostMapping
	public ReservationDTO createReservation(@RequestBody @Valid ReservationDTO reservation) {
		try {
			return service.createReservation(reservation);
		} catch (JpaSystemException jpae) {
			throw new InvalidReservationException("There is a reservation already being made. Try again, please.", jpae);
		} catch (Exception e) {
			return null;
		}
	}

	@PutMapping
	public ReservationDTO editReservation(@RequestBody @Valid ReservationDTO reservation) {
		try {
			return service.editReservation(reservation);
		} catch (JpaSystemException jpae) {
			throw new InvalidReservationException("There is a reservation already being made. Try again, please.", jpae);
		}
	}

	@DeleteMapping("/{reservationId}")
	public @ResponseBody void cancelReservation(@PathVariable UUID reservationId) {
		service.cancelReservation(reservationId);
	}

}
