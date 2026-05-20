package it.openbear.dummyspringboot.traffic;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DummyTrafficPlanner {

    private static final List<String> SYNTHETIC_FAILURES = List.of(
        "Synthetic upstream cache desynchronization",
        "Invented payload checksum drift",
        "Fabricated edge timeout saturation"
    );

    private final DummyTrafficProperties properties;
    private final int totalWeight;

    public DummyTrafficPlanner(DummyTrafficProperties properties) {
        this.properties = properties;
        this.totalWeight = properties.totalWeight();

        if (properties.delay().minMs() < 0 || properties.delay().maxMs() < properties.delay().minMs()) {
            throw new IllegalArgumentException("Dummy traffic delay range must be non-negative and ordered.");
        }
        if (this.totalWeight <= 0) {
            throw new IllegalArgumentException("Dummy traffic weights must sum to a positive value.");
        }
    }

    public DummyTrafficPlan plan() {
        HttpStatus status = selectStatus(ThreadLocalRandom.current().nextInt(totalWeight));
        long delayMs = ThreadLocalRandom.current().nextLong(properties.delay().minMs(), properties.delay().maxMs() + 1);

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            String message = SYNTHETIC_FAILURES.get(ThreadLocalRandom.current().nextInt(SYNTHETIC_FAILURES.size()));
            return new DummyTrafficPlan(status, delayMs, Optional.of(new SyntheticTrafficException(message)));
        }

        return new DummyTrafficPlan(status, delayMs, Optional.empty());
    }

    HttpStatus selectStatus(int roll) {
        int threshold = properties.weights().ok();
        if (roll < threshold) {
            return HttpStatus.OK;
        }

        threshold += properties.weights().accepted();
        if (roll < threshold) {
            return HttpStatus.ACCEPTED;
        }

        threshold += properties.weights().noContent();
        if (roll < threshold) {
            return HttpStatus.NO_CONTENT;
        }

        threshold += properties.weights().badRequest();
        if (roll < threshold) {
            return HttpStatus.BAD_REQUEST;
        }

        threshold += properties.weights().notFound();
        if (roll < threshold) {
            return HttpStatus.NOT_FOUND;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
