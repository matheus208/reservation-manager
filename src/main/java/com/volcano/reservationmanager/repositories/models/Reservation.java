package com.volcano.reservationmanager.repositories.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Reservation {

	@Id
	@GeneratedValue
	private UUID id;

	@Column
	private String name;

	@Column
	private String email;

	@Column
	private LocalDate startDate;

	@Column
	private LocalDate endDate;

	public Reservation() {}

	public Reservation(String name, String email, LocalDate startDate, LocalDate endDate) {
		this(null, name, email, startDate, endDate);
	}

	public Reservation(UUID id, String name, String email, LocalDate startDate, LocalDate endDate) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.startDate = startDate;
		this.endDate = endDate;
	}


	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) { this.name = name; }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {this.email = email; }

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
