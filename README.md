# Smart Campus API
A RESTful API built with JAX-RS (Jersey) to manage campus sensor networks and room analytics.

## Setup Instructions
1. Clone the repository.
2. Open the project in NetBeans.
3. Clean and Build the project (Maven will install dependencies including HK2 for dependency injection).
4. Run the project on Apache Tomcat/GlassFish.
5. Base URL: `http://localhost:8080/api/v1/`


## Conceptual Report

###  Part 1: Service Architecture & Setup 
#### Part 1.1:  Project & Application Configuration (JAX-RS Lifecycle & Data Synchronization)
**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures $(maps/lists)$ to prevent data loss or race
conditions.

**Answer:**
By default, JAX-RS treats Resource classes as **request-scoped**. This means a brand new instance of the class is created for every single incoming HTTP request, and it is destroyed as soon as the response is sent. 

Because we are using in-memory data structures (like `HashMap` or `ArrayList`) instead of a database, this request-scoped lifecycle creates a massive problem: if we store our lists directly inside the resource class, they will be deleted every time a request finishes! Furthermore, if multiple requests arrive at the same time, they could overwrite each other's data (a race condition). 

To fix this, we must use a **Singleton** data store class, or use thread-safe static structures like `ConcurrentHashMap` or `Collections.synchronizedList()` to ensure the data persists across all requests and is safely modified by multiple users at once.

#### Part 1.2: The ”Discovery” Endpoint (Hypermedia)
**Question:** Why is the provision of "Hypermedia" considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:**
HATEOAS (Hypermedia as the Engine of Application State) embeds navigational links directly within the JSON responses of the API. This transforms the API from a static data provider into a self-documenting, navigable state machine.

For client developers, this is significantly superior to static documentation because it eliminates the need to hardcode URLs on the client side. If the backend routes change in the future, the client application will not break, as it dynamically reads the correct endpoints (e.g., the URL for "rooms" or "sensors") directly from the discovery payload. It also informs the client exactly what actions are currently available based on the server's state.

###  Part 2: Room Management
#### Part 2.1: RoomResource Implementation
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**
Returning only IDs minimizes the JSON payload size, significantly reducing network bandwidth consumption and improving server response times. However, it forces the client to make subsequent, individual GET requests for each ID to retrieve the necessary metadata, leading to the "N+1 query problem" and increased network latency.

Returning full room objects requires higher initial bandwidth and more server-side memory to serialize the larger payload. The advantage is that the client receives all required data in a single network round-trip, minimizing latency and reducing the complexity of client-side data fetching logic.

#### Part 2.2: RoomDeletion & Safety Logic
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**
Yes, the DELETE operation is idempotent. Idempotency in REST means that making multiple identical requests has the same effect on the server's state as making a single request. 

If a client sends a `DELETE /rooms/LIB-301` request, the server removes the room and returns a `204 No Content` status. If the client mistakenly sends the exact same `DELETE` request five more times, the server will not find the room and will return a `404 Not Found` status. Despite the different HTTP status code returned to the client, the server's internal state remains unchanged after the first request—the room is still deleted. Therefore, the operation is strictly idempotent.

### Part 3: Sensor Operations & Linking 
#### Part 3.1: Sensor Resource & Integrity
**Question:** Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml, when `@Consumes(MediaType.APPLICATION_JSON)` is used. How does JAX-RS handle this mismatch?

**Answer:**
If a client sends a payload in an unsupported format like `text/plain` or `application/xml`, JAX-RS intercepts the request before it reaches the resource method. Because the `@Consumes` annotation explicitly restricts acceptable formats to JSON, the framework will automatically reject the request and return an HTTP `415 Unsupported Media Type` response to the client. This prevents the server from attempting to parse incompatible data structures, avoiding internal parsing exceptions and saving processing overhead.

#### Part 3.2: Filtered Retrieval & Search
**Question:** Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**
Path parameters (`/sensors/type/CO2`) define strict resource hierarchies and imply that the filter is a distinct sub-resource. This becomes rigid; if a client wants to fetch all sensors without filtering, a different route (`/sensors`) must be used. 

Query parameters (`/sensors?type=CO2`) are optional modifiers applied to a base collection. This approach is superior for filtering because it relies on a single predictable endpoint (`/sensors`). Furthermore, query parameters are highly extensible, allowing clients to easily stack multiple filters (e.g., `?type=CO2&status=ACTIVE`) without requiring the backend developer to map complex, deeply nested URL permutations.

### Part 4: Deep Nesting with Sub-Resources

#### 4.1 The Sub-Resource Locator Pattern
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

**Answer:**
The Sub-Resource Locator pattern promotes the "Separation of Concerns" principle. By delegating nested paths (e.g., `/sensors/{id}/readings`) to a dedicated class (`SensorReadingResource`), we prevent the creation of "God Classes"—massive, unmaintainable controller classes that handle too many responsibilities. This modular approach significantly improves code readability, makes unit testing focused and efficient, and allows developers to manage complex routing hierarchies without cluttering the primary resource controllers.

---

### Part 5: Advanced Error Handling & Exception Mapping

#### 5.1  Dependency Validation (422 Unprocessable Entity)
**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**
A `404 Not Found` implies that the endpoint (the URL path itself) does not exist; the server cannot find the requested resource. However, a `422 Unprocessable Entity` is semantically more accurate when the route *is* valid, but the data payload contains business logic errors (such as referencing a `roomId` that does not exist). It effectively communicates to the client: "I understand the request and I am capable of processing it, but your data is logically invalid."

#### 5.2 Cybersecurity & Stack Traces
**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:**
Exposing internal Java stack traces is a significant security risk known as **Information Leakage**. Stack traces act as a roadmap for attackers, revealing internal package hierarchies, specific library versions (which may have known vulnerabilities or CVEs), and underlying server file paths. An attacker can use this "reconnaissance" data to craft targeted injection attacks, exploit outdated dependencies, or bypass security controls. A production-ready API must always intercept these exceptions and return a sanitized, generic error message.

---

## API Testing (Curl Commands)
You can test the functionality of the API using the following sample curl commands:

1. **Discovery:**
   `curl -X GET http://localhost:8080/api/v1/`
2. **Create Room:**
   `curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d '{"id":"LIB-301", "name":"Library", "capacity":50}'`
3. **Register Sensor:**
   `curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d '{"id":"TEMP-01", "type":"Temperature", "status":"ACTIVE", "roomId":"LIB-301"}'`
4. **Add Reading:**
   `curl -X POST http://localhost:8080/api/v1/sensors/TEMP-01/readings -H "Content-Type: application/json" -d '{"value":22.5}'`
5. Trigger Conflict (Delete Room with Sensors)
   `curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301`
mpotent.
