package br.com.lztec;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// REQUISITO BANCA: teste de integracao - valida que todo o contexto Spring
// (controllers, services, repositories, security) sobe sem erro de configuracao.
// Os testes unitarios de cada regra de negocio estao em src/test/.../Service/impl/
@SpringBootTest
class LztecApplicationTests {

	@Test
	void contextLoads() {
	}

}