package com.volcano.reservationmanager.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.volcano.reservationmanager.ReservationManagerApplication;
import com.volcano.reservationmanager.models.ReservationDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ReservationManagerApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
@ActiveProfiles("test")
public class ReservationManagerApplicationIT {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void search_by_date() throws Exception {
		mockMvc.perform(get("/reservations")
							.param("from", LocalDate.now().plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE))
							.param("to", LocalDate.now().plus(2, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", is(empty())));

		//Save reservation
		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(1, ChronoUnit.DAYS),
				LocalDate.now().plus(2, ChronoUnit.DAYS)
		);

		mockMvc
			.perform(post("/reservations")
					.content(getObjectMapper().writeValueAsString(reservation))
					.header("Content-type", "application/json"))
			.andReturn();

		mockMvc.perform(get("/reservations")
				.param("from", LocalDate.now().plus(1, ChronoUnit.DAYS).toString())
				.param("to", LocalDate.now().plus(2, ChronoUnit.DAYS).toString()))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", is(not(empty()))));

	}

	@Test
	public void test_crud_reservation() throws Exception {
		//Save reservation
		ReservationDTO reservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(1, ChronoUnit.DAYS),
				LocalDate.now().plus(2, ChronoUnit.DAYS)
		);
		MvcResult mvcResult = mockMvc
				.perform(post("/reservations")
						.content(getObjectMapper().writeValueAsString(reservation))
						.header("Content-type", "application/json"))
				.andReturn();

		ReservationDTO savedReservation = getObjectMapper()
				.readerFor(ReservationDTO.class)
				.readValue(mvcResult.getResponse().getContentAsString());

		// Read reservation
		mockMvc
				.perform(get("/reservations/" + savedReservation.getId()))
				.andExpect(jsonPath("$.id", equalTo(savedReservation.getId().toString())));

		//Delete reservation
		mockMvc
				.perform(delete("/reservations/" + savedReservation.getId()))
				.andExpect(status().isOk() );


	}

	@Test
	public void validates_person_already_booked() throws Exception {
		//Save reservation
		ReservationDTO firstReservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(10, ChronoUnit.DAYS),
				LocalDate.now().plus(12, ChronoUnit.DAYS)
		);
		MvcResult mvcResult = mockMvc
				.perform(post("/reservations")
						.content(getObjectMapper().writeValueAsString(firstReservation))
						.header("Content-type", "application/json"))
				.andReturn();

		ReservationDTO secondReservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(1, ChronoUnit.DAYS),
				LocalDate.now().plus(2, ChronoUnit.DAYS)
		);
		mockMvc
				.perform(post("/reservations")
						.content(getObjectMapper().writeValueAsString(secondReservation))
						.header("Content-type", "application/json"))
				.andExpect(status().is4xxClientError());

	}

	@Test
	public void validates_date_already_booked() throws Exception {
		//Save reservation
		ReservationDTO firstReservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(10, ChronoUnit.DAYS),
				LocalDate.now().plus(12, ChronoUnit.DAYS)
		);
		MvcResult mvcResult = mockMvc
				.perform(post("/reservations")
						.content(getObjectMapper().writeValueAsString(firstReservation))
						.header("Content-type", "application/json"))
				.andReturn();

		ReservationDTO secondReservation = new ReservationDTO(
				"Anna",
				"anna@email.com",
				LocalDate.now().plus(10, ChronoUnit.DAYS),
				LocalDate.now().plus(12, ChronoUnit.DAYS)
		);
		mockMvc
				.perform(post("/reservations")
						.content(getObjectMapper().writeValueAsString(secondReservation))
						.header("Content-type", "application/json"))
				.andExpect(status().is4xxClientError());

	}

	@Test
	public void does_not_allow_two_bookings() throws Exception {
		//Check is empty
		mockMvc.perform(get("/reservations")
				.param("from", LocalDate.now().plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE))
				.param("to", LocalDate.now().plus(2, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE)))
				.andExpect(jsonPath("$.length()", is(0)));

		//Two reservations on the same date
		ReservationDTO firstReservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(1, ChronoUnit.DAYS),
				LocalDate.now().plus(2, ChronoUnit.DAYS)
		);
		ReservationDTO secondReservation = new ReservationDTO(
				"Mary",
				"mary@email.com",
				LocalDate.now().plus(1, ChronoUnit.DAYS),
				LocalDate.now().plus(2, ChronoUnit.DAYS)
		);

		//Tries to execute them at the "same time"
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.initialize();

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(
			CompletableFuture.runAsync(() -> {
				try {
					mockMvc.perform(post("/reservations")
							.content(getObjectMapper().writeValueAsString(firstReservation))
							.header("Content-type", "application/json"));
				} catch (Exception e) {
					//One of the requests failed...
				}
			}),
			CompletableFuture.runAsync(() -> {
				try {
					mockMvc.perform(post("/reservations")
							.content(getObjectMapper().writeValueAsString(secondReservation))
							.header("Content-type", "application/json"));
				} catch (Exception e) {
					//One of the requests failed...
				}
			})
		);
		allFutures.join();

		//There should be only one reservation
		allFutures.thenRun(() -> {
			try {
				MvcResult mvcResult = mockMvc.perform(get("/reservations")
						.param("from", LocalDate.now().plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE))
						.param("to", LocalDate.now().plus(2, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.length()", is(1)))
						.andReturn();

				List<ReservationDTO> savedReservations = getObjectMapper()
						.readerFor(new TypeReference<List<ReservationDTO>>() {})
						.readValue(mvcResult.getResponse().getContentAsString());
				for (ReservationDTO r : savedReservations) {
					mockMvc.perform(delete("/reservations/{id}", r.getId()));
				}

			} catch (Exception e) {
				Assertions.fail(e);
			}
		})
		.join();

	}

	@Test
	public void edit_reservation() throws Exception {
		//create reservation
		ReservationDTO firstReservation = new ReservationDTO(
				"Ann",
				"ann@email.com",
				LocalDate.now().plus(10, ChronoUnit.DAYS),
				LocalDate.now().plus(12, ChronoUnit.DAYS)
		);
		MvcResult mvcResult = mockMvc.perform(post("/reservations")
				.content(getObjectMapper().writeValueAsString(firstReservation))
				.header("Content-type", "application/json"))
				.andExpect(status().isOk())
				.andReturn();

		ReservationDTO savedReservation = getObjectMapper()
				.readerFor(ReservationDTO.class)
				.readValue(mvcResult.getResponse().getContentAsString());

		//edit reservation
		ReservationDTO editedReservation = new ReservationDTO(
				savedReservation.getId(),
				"Ann",
				"ann@email.com",
				LocalDate.now().plus(11, ChronoUnit.DAYS),
				LocalDate.now().plus(13, ChronoUnit.DAYS)
		);
		mockMvc.perform(put("/reservations")
				.content(getObjectMapper().writeValueAsString(editedReservation))
				.header("Content-type", "application/json"))
				.andExpect(status().isOk())
				.andReturn();

		//get reservation
		MvcResult mvcResult2 = mockMvc.perform(get("/reservations/{id}", savedReservation.getId())
				.header("Content-type", "application/json"))
				.andExpect(status().isOk())
				.andReturn();

		ReservationDTO finalReservation = getObjectMapper()
				.readerFor(ReservationDTO.class)
				.readValue(mvcResult2.getResponse().getContentAsString());

		Assertions.assertEquals(editedReservation, finalReservation);
	}

	@Test
	public void edit_reservation_to_reserved_date() throws Exception {
		//create reservation
		ReservationDTO firstReservation = new ReservationDTO(
				"Matheus",
				"matheus@email.com",
				LocalDate.now().plus(5, ChronoUnit.DAYS),
				LocalDate.now().plus(8, ChronoUnit.DAYS)
		);
		MvcResult mvcResult = mockMvc.perform(post("/reservations")
				.content(getObjectMapper().writeValueAsString(firstReservation))
				.header("Content-type", "application/json"))
				.andExpect(status().isOk())
				.andReturn();

		ReservationDTO secondReservation = new ReservationDTO(
				"Matheus2",
				"matheus2@email.com",
				LocalDate.now().plus(10, ChronoUnit.DAYS),
				LocalDate.now().plus(12, ChronoUnit.DAYS)
		);
		MvcResult mvcResult2 = mockMvc.perform(post("/reservations")
				.content(getObjectMapper().writeValueAsString(secondReservation))
				.header("Content-type", "application/json"))
				.andExpect(status().isOk())
				.andReturn();

		ReservationDTO savedReservation = getObjectMapper()
				.readerFor(ReservationDTO.class)
				.readValue(mvcResult2.getResponse().getContentAsString());

		//edit reservation
		ReservationDTO editedReservation = new ReservationDTO(
				savedReservation.getId(),
				"Matheus2",
				"matheus2@email.com",
				LocalDate.now().plus(5, ChronoUnit.DAYS),
				LocalDate.now().plus(8, ChronoUnit.DAYS)
		);
		mockMvc.perform(put("/reservations")
				.content(getObjectMapper().writeValueAsString(editedReservation))
				.header("Content-type", "application/json"))
				.andExpect(status().isBadRequest())
				.andReturn();
	}

	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		JavaTimeModule module = new JavaTimeModule();
		module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE));

		objectMapper.registerModule(module);

		return objectMapper;
	}
}

