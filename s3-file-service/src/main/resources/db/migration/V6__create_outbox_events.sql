CREATE TABLE IF NOT EXISTS outbox_events
(
    id            BIGSERIAL PRIMARY KEY,
    event_type    VARCHAR(20)              NOT NULL,
    payload       JSONB                    NOT NULL,
    status        VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    retry_count   INT                      NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    sent_at       TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE outbox_events IS 'Outbox таблица для гарантированной доставки событий в Kafka';
COMMENT ON COLUMN outbox_events.event_type IS 'Тип события: FILE_UPLOAD';
COMMENT ON COLUMN outbox_events.payload IS 'Сериализованное событие в формате JSON';
COMMENT ON COLUMN outbox_events.status IS 'Статус доставки: PENDING, SENT, FAILED';
COMMENT ON COLUMN outbox_events.retry_count IS 'Количество попыток отправки';
COMMENT ON COLUMN outbox_events.next_retry_at IS 'Время следующей попытки отправки';
COMMENT ON COLUMN outbox_events.created_at IS 'Время создания события';
COMMENT ON COLUMN outbox_events.sent_at IS 'Время успешной отправки в Kafka';

CREATE INDEX idx_outbox_events_status ON outbox_events (status);
CREATE INDEX idx_outbox_events_next_retry_at ON outbox_events (next_retry_at);