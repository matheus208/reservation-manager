package com.volcano.reservationmanager.models;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class ReservationDTO implements Serializable {

	private static final Long serialVersionUID = 1L;

	private UUID id;

	@NotNull @NotEmpty
	private String name;

	@NotNull @NotEmpty
	private String email;

	@NotNull @Future
	private LocalDate from;

	@NotNull @Future
	private LocalDate to;

	public ReservationDTO() {
	}

	public ReservationDTO(String name, String email, LocalDate from, LocalDate to) {
		this(null, name, email, from, to);
	}

	public ReservationDTO(UUID id, String name, String email, LocalDate from, LocalDate to) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.from = from;
		this.to = to;

	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public LocalDate getFrom() {
		return from;
	}

	public LocalDate getTo() {
		return to;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!this.getClass().equals(o.getClass())) {
			return false;
		}
		ReservationDTO other = (ReservationDTO) o;
		return other.getId().equals(this.id)
				&& other.getFrom().equals(this.from)
				&& other.getTo().equals(this.to)
				&& other.getName().equals(this.name)
				&& other.getEmail().equals(this.email);
	}
}
