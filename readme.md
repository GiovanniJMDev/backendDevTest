# Backend dev technical test
We want to offer a new feature to our customers showing similar products to the one they are currently seeing. To do this we agreed with our front-end applications to create a new REST API operation that will provide them the product detail of the similar products for a given one. [Here](./similarProducts.yaml) is the contract we agreed.

We already have an endpoint that provides the product Ids similar for a given one. We also have another endpoint that returns the product detail by product Id. [Here](./existingApis.yaml) is the documentation of the existing APIs.

**Create a Spring boot application that exposes the agreed REST API on port 5000.**

![Diagram](./assets/diagram.jpg "Diagram")

Note that _Test_ and _Mocks_ components are given, you must only implement _yourApp_.

## Testing and Self-evaluation
You can run the same test we will put through your application. You just need to have docker installed.

First of all, you may need to enable file sharing for the `shared` folder on your docker dashboard -> settings -> resources -> file sharing.

Then you can start the mocks and other needed infrastructure with the following command.
```
docker-compose up -d simulado influxdb grafana
```
Check that mocks are working with a sample request to [http://localhost:3001/product/1/similarids](http://localhost:3001/product/1/similarids).

To execute the test run:
```
docker-compose run --rm k6 run scripts/test.js
```
Browse [http://localhost:3000/d/Le2Ku9NMk/k6-performance-test](http://localhost:3000/d/Le2Ku9NMk/k6-performance-test) to view the results.

## Evaluation
The following topics will be considered:
- Code clarity and maintainability
- Performance
- Resilience

## Development

### How it works
`GET /product/{productId}/similar` asks the existing API for the similar product ids and then fetches the detail of each one **in parallel** (virtual threads), keeping the similarity order.

- Calls to the external API have a timeout (`products.api.timeout`, 3s by default).
- If a similar product fails (404, 500 or timeout) it is left out of the response instead of failing the whole request.
- If the requested product does not exist the API returns `404`; if the similar-ids call itself fails (5xx, timeout, connection) it returns `502`.
- The external payloads are read as generic lists/maps (`ExternalDataDto`), so new or unexpected fields in the external API do not break the application. Only the response keeps a typed DTO, as defined in [similarProducts.yaml](./similarProducts.yaml).

### Running
```
docker-compose up -d simulado influxdb grafana
set -a; . ./.env; set +a   # optional: loads SERVER_PORT (5050 on macOS)
./mvnw spring-boot:run
docker-compose run --rm k6 run scripts/test.js
```

### API collection (Bruno)
The [http/](./http) folder contains a [Bruno](https://www.usebruno.com/) collection with the endpoints of the application and of the external mocks, plus a `local` environment (`host`, `mocksHost` and `productId`). On macOS, change `host` to your `SERVER_PORT` if you do not use 5000.

I used Bruno as an alternative to Postman because it is the tool I usually work with: it is open source and free, and it is available both as a desktop application and as an IDE extension (VS Code).

### Tests and code quality
```
./mvnw clean test
```
Tests do not need Docker or the mocks. The JaCoCo report is generated in `target/site/jacoco`.

To run the SonarQube analysis, start the server with `docker-compose up -d sonarqube`, create a token at [http://localhost:9000](http://localhost:9000) and set it in `.env` (see `.env.example`). Then run the VS Code task **SonarQube: Run Analysis**.

### Decision: virtual threads and a 3-second timeout
Tomcat runs on virtual threads (`spring.threads.virtual.enabled=true`). Each request spends most of its time waiting for the external API, and with the default 200 platform threads the 200 virtual users of the load test would exhaust the pool as soon as the slow products appear.

The external calls have a 3-second timeout (`products.api.timeout`). The mocks include products that take 1s, 5s and 50s, and waiting for them would block every request for that long. With the timeout, the 1s product is still returned while the 5s and 50s ones are left out of the response, like any other failing product. The trade-off is a response with fewer products in exchange for a bounded latency; the value is configuration, so it can be raised if complete responses matter more than latency.

### Decision: port 5000 by default, configurable with `SERVER_PORT`
The statement requires port 5000, so the app uses it by default (`server.port=${SERVER_PORT:5000}`) and the k6 test targets it by default too.

On my macOS port 5000 is taken by the AirPlay Receiver (`ControlCenter`), and requests to it return 403. That is why the port can be changed without touching the code: for local development, set `SERVER_PORT=5050` in the `.env` file (see `.env.example`).
- The app reads it from the environment. VS Code loads it through the `envFile` of `launch.json`; from a terminal, load it with `set -a; . ./.env; set +a`.
- `docker-compose` reads the same `.env` and passes it to k6 as `APP_PORT`, so `shared/k6/test.js` hits the right port with no changes.

### Decision: 5-minute cache
Responses from the external API (similar ids and product detail) are kept in an in-memory cache ([Caffeine](https://github.com/ben-manes/caffeine)) for **5 minutes** (`products.cache.ttl`, or the `PRODUCTS_CACHE_TTL` environment variable).

This value is a provisional decision: we do not know yet whether, in the real case, the data changes more or less often, so a shorter TTL (fresher data) or a longer one (less load on the external API) might fit better. Since it is configuration, it can be tuned without touching the code.

Relevant behavior:
- Failed calls (404, 500, timeout) are **not** cached; they are retried on the next request.
- If several requests ask for the same data at the same time, only one of them calls the external API and they all share its result (or its failure).
- At most 10,000 entries per cache; beyond that the least used ones are evicted.
- A TTL of `0` disables the cache. Tests use that value (`src/test/resources/application.properties`) so they do not depend on it; the cache tests create the service with their own TTL.
