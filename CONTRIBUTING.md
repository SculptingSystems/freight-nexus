# Contributing

## Setup

Requires Java 21, Maven 3.9+, Docker Desktop.

```bash
git clone https://github.com/SculptingSystems/freight-nexus.git
cd freight-nexus
docker compose up -d db
mvn spring-boot:run
```

Tests require the database running:

```bash
mvn test
```

## Branching

```bash
git checkout -b feat/your-feature
# make changes
git push origin feat/your-feature
# open pull request
```

## Conventions

- Constructor injection only, no field `@Autowired`
- DTOs at the API boundary, never expose JPA entities in responses
- New database columns via a new Flyway migration â€” never edit an applied one
- HOS validation must be preserved on any change to load assignment
- Every new endpoint needs at least one integration test

## Reporting vulnerabilities

See [SECURITY.md](SECURITY.md).

