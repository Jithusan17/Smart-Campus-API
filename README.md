# Smart Campus Sensor & Room Management API

A JAX-RS (Jersey) RESTful API for managing campus rooms and IoT sensors.

## How to Build and Run

### Prerequisites
- Java 11 or higher
- Apache Maven 3.6+

### Build
```
mvn clean package
```

### Run
```
java -jar target/smart-campus-api-1.0.0.jar
```

Server starts at: http://localhost:8080/api/v1/

Press ENTER to stop the server.

---

## Sample curl Commands

### 1. Discovery
```
curl -X GET http://localhost:8080/api/v1/
```

### 2. Get all rooms
```
curl -X GET http://localhost:8080/api/v1/rooms
```

### 3. Create a room
```
curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d '{"id":"CS-201","name":"Seminar Room","capacity":25}'
```

### 4. Add a sensor
```
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d '{"id":"HUM-001","type":"Humidity","status":"ACTIVE","currentValue":55.0,"roomId":"CS-201"}'
```

### 5. Add a reading
```
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d '{"value":23.4}'
```

---

## Report — Answers to Questions

### Part 1.1 — JAX-RS Resource Class Lifecycle
By default JAX-RS creates a new instance of each resource class for every incoming HTTP request. This means instance fields are reset after every request and cannot hold shared state. To safely share data across requests all mutable data lives in a singleton DataStore class backed by ConcurrentHashMap. ConcurrentHashMap provides thread-safe reads and writes without explicit synchronisation blocks, preventing race conditions and data loss across concurrent requests.

### Part 1.2 — HATEOAS
HATEOAS means embedding navigable links directly inside API responses. A client can start at the discovery endpoint and follow links to find all other resources without reading external documentation. This decouples clients from hard-coded URLs — if a path changes on the server, a link-following client adapts automatically. It makes the API self-describing and easier to evolve without breaking existing consumers.

### Part 2.1 — Returning IDs vs Full Objects
Returning only IDs forces the client to make N additional GET requests to retrieve usable data, causing the N+1 problem which multiplies network round trips. Returning full objects risks over-fetching, sending fields the client does not need. The best practice is to return lightweight summary objects in list responses and reserve full detail for the individual GET /{id} endpoint.

### Part 2.2 — DELETE Idempotency
REST defines DELETE as idempotent meaning the server state after multiple identical requests must be the same as after a single request. In this implementation the first DELETE removes the room and returns 204 No Content. A second identical request returns 404 Not Found because the room is already gone. The server state is identical in both cases — the room is absent — which satisfies the RFC 7231 definition of idempotency. Idempotency concerns state not response codes.

### Part 3.1 — @Consumes and Media Type Mismatches
The @Consumes(MediaType.APPLICATION_JSON) annotation declares that the POST method only accepts requests with Content-Type: application/json. If a client sends text/plain or application/xml, JAX-RS intercepts the request before the method body executes and automatically returns 415 Unsupported Media Type. No explicit content-type checking code is needed in the method itself.

### Part 3.2 — @QueryParam vs Path Segment for Filtering
Using @QueryParam ?type=CO2 is correct because query parameters express refinements of a collection, not a new resource identity. A path like /sensors/type/CO2 wrongly implies a distinct hierarchical resource. Query parameters are optional by design so omitting them returns the full unfiltered collection. They are also composable, allowing multiple filters like ?type=CO2&status=ACTIVE, and are cache-friendly because HTTP caches can store filtered responses under their distinct URLs.

### Part 4.1 — Sub-Resource Locator Pattern
The Sub-Resource Locator pattern delegates request handling to a dedicated class by returning an object instance from a path-annotated method. SensorResource contains a locator for /{sensorId}/readings that returns a SensorReadingResource pre-loaded with the resolved sensor. Benefits include single responsibility where each class handles one resource level, independent testability, and scalability where adding new nesting levels requires only a new class with no changes to existing ones. A 50-line focused class is far easier to maintain than a 500-line monolithic controller.

### Part 5.2 — HTTP 422 vs 404 for Missing References
A 404 Not Found signals that the requested URI does not identify a known resource. When a client posts a sensor with an invalid roomId the URI /api/v1/sensors is perfectly valid. HTTP 422 Unprocessable Entity means the request is syntactically correct JSON delivered to a valid endpoint but contains a semantic error — a broken foreign key reference inside the payload. Returning 422 helps developers immediately distinguish a missing endpoint from a bad data dependency, leading to faster diagnosis and correct error handling.

### Part 5.4 — Security Risks of Exposing Stack Traces
Exposing raw Java stack traces leaks framework and library versions allowing attackers to look up known CVEs. It reveals internal package and class structure making targeted attacks easier. File paths and line numbers expose server directory structure. SQL exceptions include full queries revealing table names and schema. Stack traces also show code execution flow helping attackers craft inputs that reach vulnerable branches. The correct practice is to log the full trace server-side and return a generic 500 message to the client.

### Part 5.5 — Filters vs Inline Logging
Using JAX-RS filters for logging is better than inserting Logger calls in every method because one filter covers every endpoint automatically with no risk of a developer forgetting to add logging to a new method. It separates logging from business logic keeping resource classes clean and readable. Changing the log format or switching logging libraries requires editing exactly one file instead of every resource class.
