CREATE DATABASE sistema_hotel;

-- Ejecutar las siguientes sentencias conectado a la base de datos sistema_hotel.

CREATE TABLE hoteles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_hoteles_ruc CHECK (ruc ~ '^[0-9]{11}$')
);

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    hotel_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    CONSTRAINT fk_usuarios_hotel
        FOREIGN KEY (hotel_id) REFERENCES hoteles(id) ON DELETE CASCADE,
    CONSTRAINT uq_usuario_por_hotel UNIQUE (hotel_id, username),
    CONSTRAINT chk_usuarios_rol
        CHECK (rol IN ('ADMINISTRADOR', 'RECEPCIONISTA'))
);

CREATE TABLE habitaciones (
    id SERIAL PRIMARY KEY,
    hotel_id INT NOT NULL,
    numero VARCHAR(20) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    precio_por_noche NUMERIC(10, 2) NOT NULL,
    cantidad_camas INT NOT NULL DEFAULT 1,
    tiene_bano_privado BOOLEAN NOT NULL DEFAULT FALSE,
    tiene_tv BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_habitaciones_hotel
        FOREIGN KEY (hotel_id) REFERENCES hoteles(id) ON DELETE CASCADE,
    CONSTRAINT uq_habitacion_por_hotel UNIQUE (hotel_id, numero),
    CONSTRAINT uq_habitacion_tenant_id UNIQUE (hotel_id, id),
    CONSTRAINT chk_habitaciones_tipo
        CHECK (tipo IN ('INDIVIDUAL', 'DOBLE', 'MATRIMONIAL', 'FAMILIAR')),
    CONSTRAINT chk_habitaciones_precio CHECK (precio_por_noche >= 0),
    CONSTRAINT chk_habitaciones_camas CHECK (cantidad_camas > 0),
    CONSTRAINT chk_habitaciones_estado
        CHECK (estado IN ('DISPONIBLE', 'OCUPADA', 'EN_LIMPIEZA', 'MANTENIMIENTO'))
);

CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    hotel_id INT NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    documento_identidad VARCHAR(20) NOT NULL,
    telefono VARCHAR(20),
    CONSTRAINT fk_clientes_hotel
        FOREIGN KEY (hotel_id) REFERENCES hoteles(id) ON DELETE CASCADE,
    CONSTRAINT uq_cliente_por_hotel UNIQUE (hotel_id, documento_identidad),
    CONSTRAINT uq_cliente_tenant_id UNIQUE (hotel_id, id)
);

CREATE TABLE reservas (
    id SERIAL PRIMARY KEY,
    hotel_id INT NOT NULL,
    habitacion_id INT NOT NULL,
    cliente_id INT NOT NULL,
    fecha_ingreso TIMESTAMP NOT NULL,
    fecha_salida TIMESTAMP NOT NULL,
    total_pagado NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    estado_reserva VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_reservas_hotel
        FOREIGN KEY (hotel_id) REFERENCES hoteles(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservas_habitacion_tenant
        FOREIGN KEY (hotel_id, habitacion_id)
        REFERENCES habitaciones(hotel_id, id),
    CONSTRAINT fk_reservas_cliente_tenant
        FOREIGN KEY (hotel_id, cliente_id)
        REFERENCES clientes(hotel_id, id),
    CONSTRAINT uq_reserva_tenant_id UNIQUE (hotel_id, id),
    CONSTRAINT chk_reservas_fechas CHECK (fecha_salida > fecha_ingreso),
    CONSTRAINT chk_reservas_total CHECK (total_pagado >= 0),
    CONSTRAINT chk_reservas_estado
        CHECK (estado_reserva IN ('ACTIVA', 'FINALIZADA', 'CANCELADA'))
);

CREATE INDEX idx_usuarios_hotel ON usuarios(hotel_id);
CREATE INDEX idx_habitaciones_hotel ON habitaciones(hotel_id);
CREATE INDEX idx_clientes_hotel ON clientes(hotel_id);
CREATE INDEX idx_reservas_hotel ON reservas(hotel_id);
CREATE INDEX idx_reservas_habitacion ON reservas(hotel_id, habitacion_id);
CREATE INDEX idx_reservas_cliente ON reservas(hotel_id, cliente_id);

INSERT INTO hoteles (nombre, ruc, direccion) VALUES
('Hotel Central', '20123456789', 'Av. Principal 1000, Lima'),
('Hostal del Sol', '20987654321', 'Calle Las Magnolias 250, Arequipa'),
('Resort Las Dunas', '20456123789', 'Panamericana Sur Km 300, Ica');

INSERT INTO usuarios (hotel_id, username, password, rol) VALUES
(1, 'jadmin_central', 'root', 'ADMINISTRADOR'),
(1, 'mrecep_central', 'root', 'RECEPCIONISTA'),
(2, 'admin_sol', 'root', 'ADMINISTRADOR'),
(2, 'recep_sol1', 'root', 'RECEPCIONISTA'),
(2, 'recep_sol2', 'root', 'RECEPCIONISTA'),
(3, 'admin_dunas', 'root', 'ADMINISTRADOR');
