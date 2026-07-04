# CI/CD, Fault Injection, and Automated Recovery

The CI/CD workflow should prove that the application builds, tests, packages, starts, survives a controlled failure, and recovers automatically.

## Pipeline

1. Check out the repository.
2. Set up Java 21.
3. Run `mvn test`.
4. Build the Docker image.
5. Start the application and PostgreSQL with Docker Compose.
6. Verify `/actuator/health` and `/api/products`.
7. Inject a fault by stopping the application container.
8. Restart the application container.
9. Verify health and product browsing again.
10. Tear down containers.

## Fault Injection

The workflow intentionally stops the application container. This validates that the documented deployment can restore the service and that the health endpoint becomes available after recovery.

For a production platform, the same idea would be extended with:

- Pod/container kill tests.
- Database connection interruption tests.
- Slow dependency or latency injection.
- Resource pressure tests.
- Rollback checks after failed deployment.

## Automated Recovery

In local CI, recovery is represented by restarting the Compose service. In production, the orchestrator should perform this automatically through health checks, restart policies, rolling deployments, readiness probes, and rollback rules.

## Configuration

The repository includes `.github/workflows/ci.yml` as a portable GitHub Actions example. The same stages can be translated to GitLab CI, Jenkins, Azure DevOps, or another CI/CD system.
