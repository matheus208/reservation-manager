package com.volcano.reservationmanager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidReservationException extends RuntimeException {
	public InvalidReservationException(String message) {
		super(message);
	}
	public InvalidReservationException(String message, Exception reason) {
		super(message, reason);
	}
}
