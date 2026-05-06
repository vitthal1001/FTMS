package com.neobankx.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void contextLoads() {
    }

    @Test
    void exposesOpenApiAndHealthEndpoints() {
        webTestClient.get().uri("/actuator/health/liveness").exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/v3/api-docs").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("NeoBankX API Gateway"));
    }
}
