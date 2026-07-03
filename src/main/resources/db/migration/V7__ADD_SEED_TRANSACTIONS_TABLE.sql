INSERT INTO transactions
(id, business_id, account_id, amount, currency, direction, description, balance_after, created_at, updated_at)
VALUES (DEFAULT, '3ec7f001-f418-413a-84ed-9b63ee91054a',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        10.67000000, 'USD', 'IN', 'Seeded transaction', 10.67000000,
        '2026-07-03 06:36:30.309', '2026-07-03 06:36:30.309'),

       (DEFAULT, 'a5ce8763-60dc-4c8b-95bc-b442048d41fd',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 111.34000000,
        '2026-07-03 06:38:10.581', '2026-07-03 06:38:10.582'),

       (DEFAULT, 'efdb3e1d-f4d8-4a39-9140-4d9483ae852b',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 212.01000000,
        '2026-07-03 06:38:10.608', '2026-07-03 06:38:10.608'),

       (DEFAULT, '08cbc757-6e4b-4e63-9e76-309b17172dbc',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 312.68000000,
        '2026-07-03 06:38:10.640', '2026-07-03 06:38:10.640'),

       (DEFAULT, '325ef197-fe4a-4086-8edc-d1303b7a318b',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 413.35000000,
        '2026-07-03 06:38:10.675', '2026-07-03 06:38:10.675'),

       (DEFAULT, '1e2d0f2a-c308-4148-954e-8dd9415a9ee3',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 514.02000000,
        '2026-07-03 06:38:10.686', '2026-07-03 06:38:10.686'),

       (DEFAULT, 'de1665db-bbc7-4f69-b776-bb119a113d12',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 614.69000000,
        '2026-07-03 06:38:10.706', '2026-07-03 06:38:10.706'),

       (DEFAULT, '66b7a6a4-d8a4-42c5-831e-f9d7a9cc63e3',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 715.36000000,
        '2026-07-03 06:38:10.718', '2026-07-03 06:38:10.718'),

       (DEFAULT, '000a5576-0286-46ab-8de5-739befa7b090',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 816.03000000,
        '2026-07-03 06:38:10.729', '2026-07-03 06:38:10.729'),

       (DEFAULT, 'd1e3249b-9db0-4789-a48c-749348282f7a',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 916.70000000,
        '2026-07-03 06:38:10.741', '2026-07-03 06:38:10.741'),

       (DEFAULT, '53dce290-599d-4d9a-a765-e24f6b0d1546',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        100.67000000, 'USD', 'IN', 'Seeded transaction', 1017.37000000,
        '2026-07-03 06:38:10.773', '2026-07-03 06:38:10.773');