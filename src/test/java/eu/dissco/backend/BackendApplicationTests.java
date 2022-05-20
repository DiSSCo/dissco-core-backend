package eu.dissco.backend;

import static org.assertj.core.api.Assertions.assertThat;

import net.cnri.cordra.api.CordraClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {
	@Autowired
	private CordraClient client;

	@Test
	void contextLoads() {
		assertThat(client).isNotNull();
	}

}
