CREATE TABLE IF NOT EXISTS transacciones (
    id BIGINT PRIMARY KEY,
    fecha DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    tipo VARCHAR(50) NOT NULL
);


CREATE TABLE IF NOT EXISTS intereses (
    cuenta_id BIGINT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    edad INT NOT NULL,
    tipo VARCHAR(50) NOT NULL
);


CREATE TABLE IF NOT EXISTS cuentas_anuales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    transaccion VARCHAR(50) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS resumen_transacciones_diarias (
    fecha DATE PRIMARY KEY,
    cantidad_transacciones INT NOT NULL,
    total_creditos DECIMAL(15,2) NOT NULL,
    total_debitos DECIMAL(15,2) NOT NULL,
    monto_total DECIMAL(15,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS resumen_cuentas_anuales (
    cuenta_id BIGINT PRIMARY KEY,
    cantidad_transacciones INT NOT NULL,
    total_depositos DECIMAL(15,2) NOT NULL,
    total_retiros DECIMAL(15,2) NOT NULL,
    saldo_anual DECIMAL(15,2) NOT NULL
);