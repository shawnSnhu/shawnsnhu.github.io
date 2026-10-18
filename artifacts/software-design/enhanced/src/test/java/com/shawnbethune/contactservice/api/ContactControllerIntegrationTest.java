package com.shawnbethune.contactservice.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ContactControllerIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Test
    void restEndpointsSupportCompleteCrudFlow() {
        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateContactRequest(
                        "REST1", "Shawn", "Bethune", "1234567890", "123 Main St"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/contacts/REST1")
                .expectBody()
                .jsonPath("$.contactId").isEqualTo("REST1");

        client.get().uri("/api/contacts/REST1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Shawn");

        client.get().uri("/api/contacts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.contactId == 'REST1')]").isNotEmpty();

        client.put().uri("/api/contacts/REST1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateContactRequest(
                        "Updated", "Person", "0987654321", "456 New St"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Updated")
                .jsonPath("$.phone").isEqualTo("0987654321");

        client.delete().uri("/api/contacts/REST1")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        client.get().uri("/api/contacts/REST1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void duplicateIdReturnsConflict() {
        CreateContactRequest request = new CreateContactRequest(
                "REST2", "Shawn", "Bethune", "1234567890", "123 Main St");

        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody();

        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void rejectedUpdateLeavesOriginalContactUnchanged() {
        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateContactRequest(
                        "REST3", "Original", "Person", "1234567890", "123 Main St"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody();

        client.put().uri("/api/contacts/REST3")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateContactRequest(
                        "Changed", "Person", "abcdefghij", "456 New St"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fieldErrors.phone").isEqualTo("Phone must contain exactly 10 digits");

        client.get().uri("/api/contacts/REST3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Original")
                .jsonPath("$.phone").isEqualTo("1234567890")
                .jsonPath("$.address").isEqualTo("123 Main St");
    }

    @Test
    void malformedAndMissingRequestsReturnBadRequestOrNotFound() {
        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{not-valid-json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Request body is missing or malformed");

        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"contactId\":\"BAD1\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

        client.get().uri("/api/contacts/UNKNOWN")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void urlUnsafeContactIdIsRejectedBeforeStorage() {
        client.post().uri("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateContactRequest(
                        "A/B", "Shawn", "Bethune", "1234567890", "123 Main St"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fieldErrors.contactId")
                .isEqualTo("Contact ID may contain only letters, numbers, underscores, and hyphens");

        client.get().uri("/api/contacts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.contactId == 'A/B')]").isEmpty();
    }
}
