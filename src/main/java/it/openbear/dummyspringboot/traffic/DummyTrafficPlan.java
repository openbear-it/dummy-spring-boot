package it.openbear.dummyspringboot.traffic;

import java.util.Optional;

import org.springframework.http.HttpStatus;

public record DummyTrafficPlan(
    HttpStatus status,
    long delayMs,
    Optional<SyntheticTrafficException> syntheticException
) {
}
