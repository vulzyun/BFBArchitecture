package com.bfb;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Integration test - requires PostgreSQL connection")
class BfbManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
