CREATE TABLE IF NOT EXISTS tipo_maquina (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE,
    fabricante_padrao TEXT,
    vida_util_anos INTEGER NOT NULL DEFAULT 5
);

CREATE TABLE IF NOT EXISTS tipo_tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    especialidade TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS peca (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE,
    descricao TEXT,
    categoria TEXT NOT NULL DEFAULT 'Geral',
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 5,
    valor_unitario REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tipo_maquina_peca (
    tipo_maquina_id INTEGER NOT NULL,
    peca_id INTEGER NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (tipo_maquina_id, peca_id),
    FOREIGN KEY (tipo_maquina_id) REFERENCES tipo_maquina(id) ON DELETE CASCADE,
    FOREIGN KEY (peca_id) REFERENCES peca(id)
);

CREATE TABLE IF NOT EXISTS maquina (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_serie TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'ATIVA',
    data_aquisicao TEXT NOT NULL,
    valor_aquisicao REAL NOT NULL DEFAULT 0,
    url_foto TEXT,
    data_base_depreciacao TEXT,
    vida_util_atual_anos INTEGER,
    tipo_maquina_id INTEGER NOT NULL,
    FOREIGN KEY (tipo_maquina_id) REFERENCES tipo_maquina(id)
);

CREATE TABLE IF NOT EXISTS tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    telefone TEXT,
    url_foto TEXT,
    disponivel INTEGER NOT NULL DEFAULT 1,
    tipo_tecnico_id INTEGER NOT NULL,
    FOREIGN KEY (tipo_tecnico_id) REFERENCES tipo_tecnico(id)
);

CREATE TABLE IF NOT EXISTS falha (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_hora TEXT NOT NULL,
    descricao TEXT NOT NULL,
    maquina_id INTEGER NOT NULL,
    FOREIGN KEY (maquina_id) REFERENCES maquina(id)
);

CREATE TABLE IF NOT EXISTS manutencao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'ABERTA',
    custo REAL NOT NULL DEFAULT 0,
    tecnico_id INTEGER,
    maquina_id INTEGER NOT NULL,
    tipo TEXT NOT NULL,
    descricao TEXT,
    horas_trabalhadas REAL NOT NULL DEFAULT 0,
    causa_falha TEXT,
    gravidade TEXT,
    periodicidade_dias INTEGER,
    proxima_data TEXT,
    FOREIGN KEY (tecnico_id) REFERENCES tecnico(id),
    FOREIGN KEY (maquina_id) REFERENCES maquina(id)
);

CREATE TABLE IF NOT EXISTS manutencao_peca (
    manutencao_id INTEGER NOT NULL,
    peca_id INTEGER NOT NULL,
    quantidade INTEGER NOT NULL,
    PRIMARY KEY (manutencao_id, peca_id),
    FOREIGN KEY (manutencao_id) REFERENCES manutencao(id) ON DELETE CASCADE,
    FOREIGN KEY (peca_id) REFERENCES peca(id)
);

CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    login TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    perfil TEXT NOT NULL
);

INSERT OR IGNORE INTO usuario (login, senha, perfil) VALUES ('admin', 'admin123', 'ADM');
INSERT OR IGNORE INTO usuario (login, senha, perfil) VALUES ('operador', 'op123', 'OPERADOR');