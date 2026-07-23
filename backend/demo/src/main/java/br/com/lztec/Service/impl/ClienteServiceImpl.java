package br.com.lztec.Service.impl;

import br.com.lztec.model.Cliente;
import br.com.lztec.repository.ClienteRepository;
import br.com.lztec.Service.ClienteService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Monta o campo "endereco" (texto concatenado) a partir dos campos
     * detalhados (logradouro, numero, complemento, bairro, municipio, uf,
     * cep), usados apenas para clientes Pessoa Jurídica (CNPJ). Se nenhum
     * campo detalhado foi informado (caso de Pessoa Física/CPF, que usa
     * "endereco" como texto livre direto), não altera nada.
     */
    private void atualizarEnderecoConcatenado(Cliente cliente) {
        boolean temAlgumCampoDetalhado = Stream.of(
                cliente.getLogradouro(), cliente.getNumero(), cliente.getComplemento(),
                cliente.getBairro(), cliente.getMunicipio(), cliente.getUf(), cliente.getCep()
        ).anyMatch(v -> v != null && !v.isBlank());

        if (!temAlgumCampoDetalhado) return;

        String enderecoMontado = Stream.of(
                        cliente.getLogradouro(), cliente.getNumero(), cliente.getComplemento(),
                        cliente.getBairro(), cliente.getMunicipio(), cliente.getUf(), cliente.getCep()
                )
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(", "));

        cliente.setEndereco(enderecoMontado);
    }

    /**
     * BUGFIX: a coluna cpf_cnpj tem UNIQUE no banco. NULL é tratado como
     * distinto entre si (permite vários), mas string vazia "" não é - dois
     * clientes sem CPF/CNPJ (frontend envia "") violavam a constraint e o
     * segundo cadastro falhava com 409. Convertendo "" para null aqui,
     * qualquer quantidade de clientes sem CPF/CNPJ pode ser cadastrada.
     */
    private void normalizarCpfCnpj(Cliente cliente) {
        String v = cliente.getCpfCnpj();
        if (v != null && v.isBlank()) {
            cliente.setCpfCnpj(null);
        }
    }

    @Override
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    public Optional<Cliente> findById(Integer id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Cliente save(Cliente cliente) {
        normalizarCpfCnpj(cliente);
        atualizarEnderecoConcatenado(cliente);
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente update(Integer id, Cliente clienteDetails) {
        // Busca o cliente existente pelo ID
        return clienteRepository.findById(id).map(clienteExistente -> {

            clienteExistente.setNome(clienteDetails.getNome());
            clienteExistente.setCpfCnpj(clienteDetails.getCpfCnpj());
            normalizarCpfCnpj(clienteExistente);
            clienteExistente.setEmail(clienteDetails.getEmail());
            clienteExistente.setContato(clienteDetails.getContato());
            clienteExistente.setEnderecoAdicional(clienteDetails.getEnderecoAdicional());
            clienteExistente.setLogradouro(clienteDetails.getLogradouro());
            clienteExistente.setNumero(clienteDetails.getNumero());
            clienteExistente.setComplemento(clienteDetails.getComplemento());
            clienteExistente.setBairro(clienteDetails.getBairro());
            clienteExistente.setMunicipio(clienteDetails.getMunicipio());
            clienteExistente.setUf(clienteDetails.getUf());
            clienteExistente.setCep(clienteDetails.getCep());

            // BUGFIX: so sobrescreve "endereco" se algo foi de fato enviado.
            // Isso evita apagar o endereço já salvo quando a tela de edição
            // não tem como reconstituir os campos detalhados (CNPJ sem
            // reconsulta) nem o campo de texto livre foi preenchido (CPF).
            if (clienteDetails.getEndereco() != null && !clienteDetails.getEndereco().isBlank()) {
                clienteExistente.setEndereco(clienteDetails.getEndereco());
            }

            atualizarEnderecoConcatenado(clienteExistente);

            return clienteRepository.save(clienteExistente);

        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        clienteRepository.deleteById(id);
    }
}
