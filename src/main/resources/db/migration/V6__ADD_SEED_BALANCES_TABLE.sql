INSERT INTO balances (id, business_id, account_id, currency, available_amount, created_at, updated_at)
VALUES (DEFAULT, '04be0ee9-7987-4fd6-808c-aa063c4897b3',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        'EUR', 0.00000000, now(), now()),

       (DEFAULT, 'da08fd27-4063-4e18-8ce5-6809a599be9a',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        'SEK', 0.00000000, now(), now()),

       (DEFAULT, '2cfa64ee-80aa-4df9-ac91-245333a2630c',
        (SELECT id FROM accounts WHERE business_id = 'b3d1c1c2-7c1a-4f3b-9a6f-1a2b3c4d5e6f'),
        'USD', 1017.37000000, now(), now()),

       (DEFAULT, 'cb364fc7-114f-46dd-84f6-0724c18a3f18',
        (SELECT id FROM accounts WHERE business_id = 'd4e2f2a3-8b2c-4d5e-9f6a-2b3c4d5e6f7a'),
        'EUR', 0.00000000, now(), now()),

       (DEFAULT, 'ba22b14f-50af-4b77-8e22-3623412a6ba1',
        (SELECT id FROM accounts WHERE business_id = 'd4e2f2a3-8b2c-4d5e-9f6a-2b3c4d5e6f7a'),
        'USD', 0.00000000, now(), now()),

       (DEFAULT, 'a9bb0f28-1f17-4098-80a3-7387e80ee589',
        (SELECT id FROM accounts WHERE business_id = 'd4e2f2a3-8b2c-4d5e-9f6a-2b3c4d5e6f7a'),
        'SEK', 0.00000000, now(), now());