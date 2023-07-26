INSERT INTO users (id, password, username) VALUES
                                               (1, 'test', 'bob'), (2, 'dev', 'bill'), (3, 'lead', 'dan');
INSERT INTO accounts (id, account_currency, amount, user_id) VALUES
                         (1, 0, 1,1), (2, 1, 1,1), (3, 2,1, 1),
                         (4, 0, 1,2), (5, 1, 1,2), (6, 2,1, 2),
                         (7, 0, 1,3), (8, 1, 1,3), (9, 2,1, 3);