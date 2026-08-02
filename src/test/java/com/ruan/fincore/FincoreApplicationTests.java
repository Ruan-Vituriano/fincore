package com.ruan.fincore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SUPABASE_DB_URL", matches = "^\\S+$")
class FincoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
