# Enhanced Contact Management Service

## Requirements

- Java 17 or newer
- Internet access during the first build so Maven can download dependencies

## Run the Tests

Open a terminal in the `enhanced` folder and run:

```
./mvnw test
```

The test suite contains 18 tests. These tests verify that alphabetic phone numbers and URL-unsafe contact IDs are rejected, and that an invalid update does not change any fields on the stored contact.

## Run the Application

In the same terminal, run:

```
./mvnw spring-boot:run
```

The service starts at `http://localhost:8080`. Contacts are stored in memory and are lost when the application stops.

Leave this terminal running and open a second terminal to send the example requests below. When finished, press `Ctrl+C` in the first terminal to stop the application.

## REST Endpoints

| Method | Path | Result |
| POST | `/api/contacts` | Create a contact |
| GET | `/api/contacts` | List all contacts, sorted by ID |
| GET | `/api/contacts/{contactId}` | Retrieve one contact |
| PUT | `/api/contacts/{contactId}` | Replace the editable contact fields |
| DELETE | `/api/contacts/{contactId}` | Delete one contact |

Example create request:

Run this in the second terminal:

```
curl -i -X POST http://localhost:8080/api/contacts \
  -H 'Content-Type: application/json' \
  -d '{
    "contactId": "C100",
    "firstName": "Shawn",
    "lastName": "Bethune",
    "phone": "1234567890",
    "address": "123 Main St"
  }'
```

Example retrieve request:

After creating contact `C100`, run this in the second terminal to see its current details:

```
curl -i http://localhost:8080/api/contacts/C100
```

This returns the contact as JSON with a `200 OK` status. Run it again after updating the contact to check the changes. If the contact does not exist, the service returns `404 Not Found`.

Example update request:

After creating contact `C100`, run this in the second terminal while the application is still running:

```
curl -i -X PUT http://localhost:8080/api/contacts/C100 \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Updated",
    "lastName": "Person",
    "phone": "0987654321",
    "address": "456 New St"
  }'
```
