package dev.jotxee.todo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=test-only-jwt-secret-with-at-least-32-characters",
		"spring.flyway.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:todo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class TodoApplicationTests {

	@Test
	void contextLoads() {
	}

}
