package br.com.lztec.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint responsavel por receber uma imagem do frontend e fazer o
 * upload para a nuvem (Cloudinary), retornando a URL publica gerada.
 *
 * Fluxo: frontend envia o arquivo via multipart/form-data ->
 * backend envia para o Cloudinary -> Cloudinary retorna a URL ->
 * backend devolve essa URL para o frontend salvar no produto.
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final Cloudinary cloudinary;

    public UploadController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping("/imagem")
    public ResponseEntity<Map<String, String>> uploadImagem(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nenhum arquivo enviado."));
        }

        // Valida tipo de arquivo (apenas imagens)
        String tipo = arquivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Apenas arquivos de imagem sao permitidos."));
        }

        // Limite de 5MB
        if (arquivo.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Imagem muito grande. Limite de 5MB."));
        }

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap("folder", "lztec/produtos")
            );

            String urlImagem = (String) resultado.get("secure_url");

            Map<String, String> resposta = new HashMap<>();
            resposta.put("url", urlImagem);
            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Falha ao enviar imagem: " + e.getMessage()));
        }
    }
}