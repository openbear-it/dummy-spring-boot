package it.openbear.dummyspringboot.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NonFunctionalTrafficFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void exposesActuatorPrometheus() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk());
    }

    @Test
    void handlesNonActuatorTrafficInCentralFilter() throws Exception {
        MvcResult result = mockMvc.perform(get("/anything"))
            .andExpect(header().string("X-Non-Functional-Traffic", "true"))
            .andExpect(header().exists("X-Request-Id"))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200, 202, 204, 400, 404, 500);

        if (status == 204) {
            assertThat(result.getResponse().getContentAsString()).isEmpty();
        } else {
            assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
            assertThat(result.getResponse().getContentAsString()).contains("\"message\":\"non-functional-traffic\"");
        }
    }
}
