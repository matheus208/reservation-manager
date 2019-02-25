package com.volcano.reservationmanager.converters;

import com.volcano.reservationmanager.models.ReservationDTO;
import com.volcano.reservationmanager.repositories.models.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationConverter {

	public ReservationDTO toDTO(Reservation reservation) {
		return new ReservationDTO(
				reservation.getId(),
				reservation.getName(),
				reservation.getEmail(),
				reservation.getStartDate(),
				reservation.getEndDate()
		);
	}

	public Reservation toModel(ReservationDTO reservation) {
		return new Reservation(
				reservation.getId(),
				reservation.getName(),
				reservation.getEmail(),
				reservation.getFrom(),
				reservation.getTo());
	}
}
