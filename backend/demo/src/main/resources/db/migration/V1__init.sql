-- Usuários do sistema com controle de acesso (RBAC)
-- role: ADMIN (acesso total) ou OPERADOR (acesso restrito)
CREATE TABLE usuario (
    id    SERIAL PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    senha VARCHAR(100) NOT NULL,
    role  VARCHAR(20)  NOT NULL DEFAULT 'OPERADOR'
        CHECK (role IN ('ADMIN', 'OPERADOR'))
);

-- Cliente: Pessoa Física (CPF) ou Pessoa Jurídica (CNPJ)
-- Pessoa Física usa "endereco" como logradouro/texto livre + numero/bairro
-- preenchidos manualmente. Pessoa Jurídica usa os campos detalhados de
-- endereço (logradouro, numero, complemento, bairro, municipio, uf, cep),
-- preenchidos automaticamente pela consulta de CNPJ; "endereco" fica com o
-- texto concatenado desses campos, mantido pelo backend.
CREATE TABLE cliente (
    id                  SERIAL PRIMARY KEY,
    nome                VARCHAR(100) NOT NULL,
    cpf_cnpj            VARCHAR(20) UNIQUE,
    email               VARCHAR(100),
    contato             VARCHAR(50),
    endereco            VARCHAR(200),
    data_nascimento     DATE,
    endereco_adicional  VARCHAR(150),
    logradouro          VARCHAR(120),
    numero              VARCHAR(20),
    complemento         VARCHAR(60),
    bairro              VARCHAR(60),
    municipio           VARCHAR(60),
    uf                  VARCHAR(2),
    cep                 VARCHAR(10)
);

-- Produto/Estoque
CREATE TABLE produto (
    id                SERIAL PRIMARY KEY,
    codigo            VARCHAR(20)   NOT NULL UNIQUE,  -- ex: PRD-00001, gerado pelo sistema
    nome              VARCHAR(150)  NOT NULL,
    descricao         TEXT,
    valor_venda       NUMERIC(10,2) NOT NULL CHECK (valor_venda >= 0),
    valor_promocional NUMERIC(10,2) CHECK (valor_promocional >= 0),
    quantidade        INT           NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    imagem_url        VARCHAR(500)
);

-- Venda: cabeçalho (cliente, data, status de pagamento)
-- status: PENDENTE (nada recebido), PARCIAL (recebeu uma entrada, falta o
-- resto) ou PAGO (recebeu o valor total)
CREATE TABLE venda (
    id                          SERIAL PRIMARY KEY,
    -- RESTRICT: impede excluir um Cliente que já tem venda registrada,
    -- preservando o histórico.
    cliente_id                  INT  NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
    data_venda                  DATE NOT NULL DEFAULT CURRENT_DATE,
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
        CHECK (status IN ('PENDENTE', 'PARCIAL', 'PAGO')),
    observacao                  TEXT,
    -- Desconto geral aplicado sobre o total da venda (soma dos itens já com
    -- seus próprios descontos), opcional.
    percentual_desconto_geral   NUMERIC(6,2)  NOT NULL DEFAULT 0 CHECK (percentual_desconto_geral >= 0),
    valor_desconto_geral        NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (valor_desconto_geral >= 0),
    -- Data prevista pro pagamento do restante, quando status = PARCIAL
    -- (ex: cliente deu uma entrada e vai pagar o resto em outro dia).
    data_vencimento_restante    DATE
);

-- Pagamentos recebidos de uma venda. Uma venda pode ter vários: dividido em
-- mais de uma forma no mesmo dia (ex: metade Pix, metade Dinheiro), ou uma
-- entrada + o restante pago depois em outra data.
CREATE TABLE venda_pagamento (
    id               SERIAL PRIMARY KEY,
    venda_id         INT NOT NULL REFERENCES venda(id) ON DELETE CASCADE,
    forma_pagamento  VARCHAR(20) NOT NULL
        CHECK (forma_pagamento IN ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO')),
    valor            NUMERIC(10,2) NOT NULL CHECK (valor > 0),
    numero_parcelas  INT NOT NULL DEFAULT 1 CHECK (numero_parcelas >= 1),
    data_pagamento   DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Itens (produtos) de uma venda - uma venda pode ter vários produtos
CREATE TABLE venda_item (
    id                   SERIAL PRIMARY KEY,
    venda_id             INT NOT NULL REFERENCES venda(id)   ON DELETE CASCADE,
    -- RESTRICT: impede excluir um Produto que já foi vendido, preservando o histórico.
    produto_id           INT NOT NULL REFERENCES produto(id) ON DELETE RESTRICT,
    quantidade           INT NOT NULL DEFAULT 1 CHECK (quantidade > 0),
    -- Valor unitário "congelado" no momento da venda (promocional, se
    -- houver, senão o valor de venda normal), para não mudar o total de
    -- vendas antigas se o preço do produto mudar depois.
    valor_unitario       NUMERIC(10,2) NOT NULL CHECK (valor_unitario >= 0),
    -- Desconto opcional aplicado só a este item (produto x quantidade)
    percentual_desconto  NUMERIC(6,2)  NOT NULL DEFAULT 0 CHECK (percentual_desconto >= 0),
    valor_desconto       NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (valor_desconto >= 0)
);

-- Auditoria: registra todas as ações realizadas no sistema
-- acao: CRIAR, EDITAR, EXCLUIR, LOGIN, LOGIN_FALHOU
-- detalhes: JSON com campos antes/depois da alteração
CREATE TABLE audit_log (
    id          BIGSERIAL    PRIMARY KEY,
    usuario     VARCHAR(100) NOT NULL,
    acao        VARCHAR(30)  NOT NULL,
    entidade    VARCHAR(50)  NOT NULL,
    entidade_id VARCHAR(50),
    detalhes    TEXT,
    data_hora   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_venda_cliente        ON venda(cliente_id);
CREATE INDEX idx_venda_status         ON venda(status);
CREATE INDEX idx_venda_data           ON venda(data_venda);
CREATE INDEX idx_venda_item_venda     ON venda_item(venda_id);
CREATE INDEX idx_venda_item_produto   ON venda_item(produto_id);
CREATE INDEX idx_venda_pagamento_venda ON venda_pagamento(venda_id);
CREATE INDEX idx_usuario_nome         ON usuario(nome);
CREATE INDEX idx_audit_usuario        ON audit_log(usuario);
CREATE INDEX idx_audit_acao           ON audit_log(acao);
CREATE INDEX idx_audit_entidade       ON audit_log(entidade);
CREATE INDEX idx_audit_data_hora      ON audit_log(data_hora DESC);
