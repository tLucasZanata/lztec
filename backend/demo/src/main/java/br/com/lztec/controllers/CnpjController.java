package br.com.lztec.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

/**
 * REQUISITO BANCA: consumo de API externa (nao e ViaCEP).
 * Controller que faz proxy para a API pública ReceitaWS.
 * Evita problemas de CORS ao chamar a API externa diretamente do browser.
 * Endpoint: GET /api/cnpj/{cnpj}
 */
@RestController
@RequestMapping("/api/cnpj")
public class CnpjController {

    private static final String RECEITAWS_URL = "https://receitaws.com.br/v1/cnpj/";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/{cnpj}")
    public ResponseEntity<String> buscarCnpj(@PathVariable String cnpj) {
        // Remove caracteres não numéricos
        String cnpjLimpo = cnpj.replaceAll("\\D", "");

        if (cnpjLimpo.length() != 14) {
            return ResponseEntity.badRequest().body("{\"erro\":\"CNPJ deve ter 14 dígitos.\"}");
        }

        try {
            // Adiciona header de User-Agent (ReceitaWS exige)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "LZTec-System/1.0");
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resposta = restTemplate.exchange(
                    RECEITAWS_URL + cnpjLimpo,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return ResponseEntity.ok(resposta.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body("{\"erro\":\"Não foi possível consultar a ReceitaWS: " + e.getMessage() + "\"}");
        }
    }
}