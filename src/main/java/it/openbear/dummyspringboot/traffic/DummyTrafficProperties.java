package it.openbear.dummyspringboot.traffic;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.dummy-traffic")
public record DummyTrafficProperties(
    Delay delay,
    Weights weights
) {

    public int totalWeight() {
        return weights.ok()
            + weights.accepted()
            + weights.noContent()
            + weights.badRequest()
            + weights.notFound()
            + weights.internalError();
    }

    public record Delay(
        long minMs,
        long maxMs
    ) {
    }

    public record Weights(
        int ok,
        int accepted,
        int noContent,
        int badRequest,
        int notFound,
        int internalError
    ) {
    }
}
