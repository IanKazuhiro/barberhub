-- Expediente inicial da barbearia (RN07), até existir a tela do UC16.
-- 0 = domingo ... 6 = sábado. Segunda a sábado das 9h às 19h; domingo fechado.
INSERT INTO expediente_barbearia (dia_semana, abertura, fechamento, fechado) VALUES
    (0, NULL,    NULL,    TRUE),
    (1, '09:00', '19:00', FALSE),
    (2, '09:00', '19:00', FALSE),
    (3, '09:00', '19:00', FALSE),
    (4, '09:00', '19:00', FALSE),
    (5, '09:00', '19:00', FALSE),
    (6, '09:00', '19:00', FALSE)
ON CONFLICT (dia_semana) DO NOTHING;
