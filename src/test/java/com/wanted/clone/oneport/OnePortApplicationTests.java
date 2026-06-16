package com.wanted.clone.oneport;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = "grpc.server.port=9091")
class OnePortApplicationTests {

    @Test
    void contextLoads() {
    }

}
