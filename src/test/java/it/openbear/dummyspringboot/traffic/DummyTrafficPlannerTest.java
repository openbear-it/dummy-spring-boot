package it.openbear.dummyspringboot.traffic;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DummyTrafficPlannerTest {

    private final DummyTrafficProperties properties = new DummyTrafficProperties(
        new DummyTrafficProperties.Delay(75, 900),
        new DummyTrafficProperties.Weights(60, 15, 10, 7, 5, 3)
    );

    private final DummyTrafficPlanner planner = new DummyTrafficPlanner(properties);

    @Test
    void mapsConfiguredRangesToExpectedStatuses() {
        assertThat(planner.selectStatus(0)).isEqualTo(HttpStatus.OK);
        assertThat(planner.selectStatus(59)).isEqualTo(HttpStatus.OK);
        assertThat(planner.selectStatus(60)).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(planner.selectStatus(74)).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(planner.selectStatus(75)).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(planner.selectStatus(84)).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(planner.selectStatus(85)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(planner.selectStatus(91)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(planner.selectStatus(92)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(planner.selectStatus(96)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(planner.selectStatus(97)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(planner.selectStatus(99)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void keepsHttp200AsMostFrequentStatus() {
        assertThat(properties.weights().ok())
            .isGreaterThan(properties.weights().accepted())
            .isGreaterThan(properties.weights().noContent())
            .isGreaterThan(properties.weights().badRequest())
            .isGreaterThan(properties.weights().notFound())
            .isGreaterThan(properties.weights().internalError());
    }
}
