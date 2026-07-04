# Seasonal Scaling

The normal expected load is around 1000 users per day. During Christmas, the business expects roughly ten times that traffic but does not want to pay for peak capacity all year.

## Proposed Infrastructure

Run the application as containers behind a load balancer on a platform that supports autoscaling, such as Kubernetes, ECS, or another managed container runtime.

- Keep the Spring Boot application stateless.
- Scale application replicas based on CPU, request rate, and latency.
- Use scheduled scaling to raise the minimum replica count before known Christmas peaks.
- Use PostgreSQL managed storage with automated backups, monitoring, and the option to increase instance size or read capacity during the season.
- Put static assets and API traffic behind a CDN/load balancer where appropriate.
- Keep database connection pools bounded so autoscaling application instances do not overload PostgreSQL.

## Cost Control

Outside the peak season, run a small baseline of application replicas and a normal-sized database. During seasonal peaks, temporarily increase minimum replicas, maximum replicas, and database capacity. After the peak, scale the settings back down.

## Reliability Controls

- Health checks on `/actuator/health`.
- Metrics from Spring Boot Actuator.
- Alerts for latency, error rate, database CPU, connection saturation, and failed order creation.
- Load testing before the season using expected x10 traffic and checkout-heavy scenarios.

## Trade-Offs

The current synchronous purchase flow is simple and reliable for the assignment. If checkout traffic grows beyond what database row locking can comfortably handle, the next step would be a reservation-based or queue-backed purchase flow. That would add operational complexity, so it is not implemented until the load profile requires it.
