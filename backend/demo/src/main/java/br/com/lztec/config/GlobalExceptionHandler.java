package br.com.lztec.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// REQUISITO BANCA: nao e possivel excluir um cadastro (Cliente, Produto)
// que ja esta vinculado a algum
// servico, para preservar o historico. Essa regra e garantida no banco via
// ON DELETE RESTRICT (ver V1__init.sql); este handler global converte o erro
// tecnico do Postgres ("violates foreign key constraint") numa mensagem
// amigavel, em vez de devolver um 500 cru para qualquer controller que tente
// excluir um registro com vinculo.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> tratarViolacaoIntegridade(DataIntegrityViolationException ex) {
        String causaRaiz = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        String mensagem = "Não é possível excluir: este registro está vinculado a outros dados do sistema.";
        if (causaRaiz != null && causaRaiz.toLowerCase().contains("foreign key")) {
            mensagem = "Não é possível excluir: este registro já está sendo usado em uma ou mais "
                    + "vendas (ou outro cadastro relacionado). Remova ou edite "
                    + "esses vínculos antes de excluir.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", mensagem));
    }
}
