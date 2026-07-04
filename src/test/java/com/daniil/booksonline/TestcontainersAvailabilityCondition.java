package com.daniil.booksonline;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

class TestcontainersAvailabilityCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
            ConditionEvaluationResult.enabled("Testcontainers Docker environment is available");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                return ENABLED;
            }
        } catch (RuntimeException | Error ex) {
            return disabled(ex);
        }
        return ConditionEvaluationResult.disabled("Testcontainers Docker environment is not available");
    }

    private ConditionEvaluationResult disabled(Throwable ex) {
        return ConditionEvaluationResult.disabled(
                "Testcontainers Docker environment is not available: " + ex.getClass().getSimpleName()
        );
    }
}
