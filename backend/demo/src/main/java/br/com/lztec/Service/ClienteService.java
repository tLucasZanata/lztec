package br.com.lztec.Service;

import br.com.lztec.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<Cliente> findAll();
    Optional<Cliente> findById(Integer id);
    Cliente save(Cliente cliente);
    Cliente update(Integer id, Cliente clienteDetails);
    void delete(Integer id);
}