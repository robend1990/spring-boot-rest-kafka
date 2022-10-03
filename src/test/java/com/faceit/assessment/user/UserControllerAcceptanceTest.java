package com.faceit.assessment.user;

import com.faceit.assessment.kafka.KafkaTestConsumer;
import com.faceit.assessment.user.domain.UserFacade;
import com.faceit.assessment.user.dto.CreateOrUpdateUserDto;
import com.faceit.assessment.user.dto.UserDto;
import com.faceit.assessment.user.event.UserCreatedEvent;
import com.faceit.assessment.user.event.UserEvent;
import com.faceit.assessment.user.event.UserRemovedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@ActiveProfiles(profiles = "test")
class UserControllerAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private KafkaTestConsumer kafkaTestConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    private final String robertEmail = "remail@gmail.com";
    private final String jenniferEmail = "jemail@gmail.com";

    @BeforeEach
    public void initialiseRestAssuredMockMvcWebApplicationContext() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        kafkaTestConsumer.resetLatch();
    }

    @Test
    @Order(1)
    public void shouldReturnEmptyList() {
        given().port(port).when().get("/users")
                .then()
                .assertThat().body("content", hasSize(0))
                .statusCode(OK.value());
    }

    @Test
    @Order(2)
    public void shouldCreateUserAndProducerDomainEvent() throws InterruptedException, JsonProcessingException {
        CreateOrUpdateUserDto robert = robert(robertEmail);
        UserDto createdUserDto = given().port(port).with().body(robert).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when().post("/users")
                .then()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.LOCATION, is(notNullValue()))
                .statusCode(CREATED.value())
                .assertThat().body("first_name", is(robert.getFirstName()))
                .assertThat().body("last_name", is(robert.getLastName()))
                .assertThat().body("country", is(robert.getCountry()))
                .assertThat().body("email", is(robert.getEmail()))
                .assertThat().body("nickname", is(robert.getNickname()))
                .assertThat().body("id", is(notNullValue()))
                .extract()
                .as(UserDto.class);

        UserCreatedEvent expectedDomainEvent = new UserCreatedEvent(createdUserDto.getId(), createdUserDto.getEmail());
        assertEventProduced(expectedDomainEvent);
    }

    @Test
    @Order(3)
    public void shouldReturnUsers() {
        CreateOrUpdateUserDto jennifer = jennifer(jenniferEmail);
        userFacade.create(jennifer);
        given().port(port).when().get("/users")
                .then()
                .assertThat().body("content", hasSize(2))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .statusCode(OK.value());
    }

    @Test
    @Order(4)
    public void shouldReturnLastPage() {
        given().port(port).when().get("/users?size=1&page=1")
                .then()
                .log().body()
                .assertThat().body("content", hasSize(1))
                .assertThat().body("totalPages", is(2))
                .assertThat().body("last", is(true))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .statusCode(OK.value());
    }

    @Test
    @Order(5)
    public void shouldReturnConflictIfUserWantsToChangeEmailToOccupied() {
        UserDto robertDto = findUserByEmailOrThrow(robertEmail);

        CreateOrUpdateUserDto robertUpdateRequest = robert(jenniferEmail);

        given().port(port).with().body(robertUpdateRequest)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when().put("/users/" + robertDto.getId())
                .then()
                .statusCode(CONFLICT.value());
    }

    @Test
    @Order(6)
    public void shouldUpdateUser() {
        UserDto robertDto = findUserByEmailOrThrow(robertEmail);

        CreateOrUpdateUserDto robertUpdateRequest = robert(robertEmail);
        robertUpdateRequest.setEmail("newRDEmail@gmail.com");
        robertUpdateRequest.setLastName("newLastName");
        robertUpdateRequest.setNickname("RD");

        given().port(port).with().body(robertUpdateRequest)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when().put("/users/" + robertDto.getId())
                .then()
                .statusCode(NO_CONTENT.value());

        robertDto = findUserByEmailOrThrow(robertUpdateRequest.getEmail());
        assertEquals(robertDto.getEmail(), robertUpdateRequest.getEmail());
        assertEquals(robertDto.getLastName(), robertUpdateRequest.getLastName());
        assertEquals(robertDto.getNickname(), robertUpdateRequest.getNickname());
    }

    @Test
    @Order(7)
    public void shouldRemoveUserAndProduceDomainEvent() throws InterruptedException, JsonProcessingException {
        UserDto jennifer = findUserByEmailOrThrow(jenniferEmail);

        given().port(port)
                .when().delete("/users/" + jennifer.getId())
                .then()
                .statusCode(NO_CONTENT.value());

        assertThrows(NoSuchElementException.class, () -> findUserByEmailOrThrow(jenniferEmail));

        UserRemovedEvent expectedDomainEvent = new UserRemovedEvent(jennifer.getId());
        assertEventProduced(expectedDomainEvent);
    }

    @Test
    @Order(8)
    public void shouldReturn404IfUserForUpdateNotFound() {
        CreateOrUpdateUserDto jennifer = jennifer(jenniferEmail);
        given().port(port).with().body(jennifer)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when().put("/users/" + UUID.randomUUID())
                .then()
                .statusCode(NOT_FOUND.value())
                .assertThat().body("message", containsString("No user with id"));
    }

    @Test
    @Order(9)
    public void shouldReturn404IfUserForDeleteNotFound() {
        given().port(port)
                .when().delete("/users/" + UUID.randomUUID())
                .then()
                .statusCode(NOT_FOUND.value())
                .assertThat().body("message", containsString("No user with id"));
    }

    private void assertEventProduced(UserEvent expectedDomainEvent) throws InterruptedException, JsonProcessingException {
        boolean eventConsumed = kafkaTestConsumer.getLatch().await(5, TimeUnit.SECONDS);
        assertTrue(eventConsumed);
        ConsumerRecord<String, String> event = kafkaTestConsumer.getEvent();
        String typeIdHeader = getTypeIdHeaderValue(event);

        assertThat(typeIdHeader, is(expectedDomainEvent.getClass().getName()));
        String key = event.key();
        UserEvent value = objectMapper.readValue(event.value(), expectedDomainEvent.getClass());
        assertThat(key, is(expectedDomainEvent.getId()));
        assertThat(value, is(expectedDomainEvent));
    }

    private String getTypeIdHeaderValue(ConsumerRecord<String, String> event) {
        return Arrays.stream(event.headers().toArray())
                .filter(header -> header.key().equalsIgnoreCase("__TypeId__"))
                .findFirst()
                .map(Header::value)
                .map(String::new)
                .orElseThrow();
    }

    private UserDto findUserByEmailOrThrow(String email) {
        return userFacade.findAll(Pageable.unpaged())
                .stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow();
    }

    private CreateOrUpdateUserDto robert(String email) {
        return CreateOrUpdateUserDto.builder()
                .firstName("Robert")
                .lastName("Drewniak")
                .nickname("rd")
                .password("passwd")
                .email(email)
                .build();
    }

    private CreateOrUpdateUserDto jennifer(String email) {
        return CreateOrUpdateUserDto.builder()
                .firstName("Jennifer")
                .lastName("Aniston")
                .nickname("ja")
                .password("passwd")
                .email(email)
                .build();
    }

    @TestConfiguration
    public static class KafkaTestConsumerConfiguration {
        @Bean
        public KafkaTestConsumer kafkaTestConsumer() {
            return new KafkaTestConsumer();
        }
    }
}
