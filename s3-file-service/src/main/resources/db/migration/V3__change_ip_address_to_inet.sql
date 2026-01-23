ALTER TABLE audit_logs
    ALTER COLUMN ip_address TYPE INET
        USING ip_address::INET;

COMMENT ON COLUMN audit_logs.ip_address IS 'Изменен тип для хранения IP адреса клиента на PostgreSQL INET';