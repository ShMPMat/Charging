# Charging

Test project with a mock REST Management System of Electric Vehicle
Charging Stations.

## Deployment

### Prerequisites

- **Java 17**;
- **Maven**;
- **Docker (or local Postgres)**;
- **Node.js**.

### Commands

Run from the project root:

1. `docker run -e POSTGRES_PASSWORD=test -e POSTGRES_USER=charging
   -e POSTGRES_DB=charging -p 5432:5432 postgres` - run Postgres;
2. `mvn spring-boot:run` - run the REST server;
3. `cd .\src\front\charging\; npm run dev` - run the front.
