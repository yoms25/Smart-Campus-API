# Smart Campus API

## Conceptual Report

### Part 1.1: JAX-RS Lifecycle & Data Synchronization
**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? How does this impact data management?

**Answer:**
By default, JAX-RS treats Resource classes as **request-scoped**. This means a brand new instance of the class is created for every single incoming HTTP request, and it is destroyed as soon as the response is sent. 

Because we are using in-memory data structures (like `HashMap` or `ArrayList`) instead of a database, this request-scoped lifecycle creates a massive problem: if we store our lists directly inside the resource class, they will be deleted every time a request finishes! Furthermore, if multiple requests arrive at the exact same time, they could overwrite each other's data (a race condition). 

To fix this, we must use a **Singleton** data store class, or use thread-safe static structures like `ConcurrentHashMap` or `Collections.synchronizedList()` to ensure the data persists across all requests and is safely modified by multiple users at once.

### Part 1.2: The Benefits of HATEOAS (Hypermedia)
**Question:** Why is the provision of "Hypermedia" considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:**
HATEOAS (Hypermedia as the Engine of Application State) embeds navigational links directly within the JSON responses of the API. This transforms the API from a static data provider into a self-documenting, navigable state machine.

For client developers, this is significantly superior to static documentation because it eliminates the need to hardcode URLs on the client side. If the backend routes change in the future, the client application will not break, as it dynamically reads the correct endpoints (e.g., the URL for "rooms" or "sensors") directly from the discovery payload. It also informs the client exactly what actions are currently available based on the server's state.

### Part 2.1: Payload Size and Network Bandwidth
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**
Returning only IDs minimizes the JSON payload size, significantly reducing network bandwidth consumption and improving server response times. However, it forces the client to make subsequent, individual GET requests for each ID to retrieve the necessary metadata, leading to the "N+1 query problem" and increased network latency.

Returning full room objects requires higher initial bandwidth and more server-side memory to serialize the larger payload. The advantage is that the client receives all required data in a single network round-trip, minimizing latency and reducing the complexity of client-side data fetching logic.

### Part 2.2: Deletion Logic & Idempotency
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**
Yes, the DELETE operation is idempotent. Idempotency in REST means that making multiple identical requests has the same effect on the server's state as making a single request. 

If a client sends a `DELETE /rooms/LIB-301` request, the server removes the room and returns a `204 No Content` status. If the client mistakenly sends the exact same `DELETE` request five more times, the server will not find the room and will return a `404 Not Found` status. Despite the different HTTP status code returned to the client, the server's internal state remains unchanged after the first request—the room is still deleted. Therefore, the operation is strictly idempotent.