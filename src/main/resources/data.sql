-- Currencies (id=1: BTC, id=2: ETH, id=3: USDT)
INSERT INTO crypto_currency (id, code, name, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (1, 'BTC', 'Bitcoin', true, 0, CURRENT_TIMESTAMP, 'system-auto');
INSERT INTO crypto_currency (id, code, name, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (2, 'ETH', 'Ethereum', true, 0, CURRENT_TIMESTAMP, 'system-auto');
INSERT INTO crypto_currency (id, code, name, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (3, 'USDT', 'Tether USD', true, 0, CURRENT_TIMESTAMP, 'system-auto');

-- Order sides (id=1: BUY, id=2: SELL)
INSERT INTO crypto_order_side (id, code, description, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (1, 'BUY', 'Buy order', true, 0, CURRENT_TIMESTAMP, 'system-auto');
INSERT INTO crypto_order_side (id, code, description, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (2, 'SELL', 'Sell order', true, 0, CURRENT_TIMESTAMP, 'system-auto');

-- Trading pairs (id=1: BTCUSDT, id=2: ETHUSDT)
INSERT INTO crypto_trading_pair (id, symbol, base_currency_id, quote_currency_id, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (1, 'BTCUSDT', 1, 3, true, 0, CURRENT_TIMESTAMP, 'system-auto');
INSERT INTO crypto_trading_pair (id, symbol, base_currency_id, quote_currency_id, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (2, 'ETHUSDT', 2, 3, true, 0, CURRENT_TIMESTAMP, 'system-auto');

-- Demo user (id=1)
INSERT INTO crypto_user (id, username, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (1, 'demo', true, 0, CURRENT_TIMESTAMP, 'system-auto');

-- Demo user wallets (currency_id references crypto_currency.id)
INSERT INTO crypto_wallet (id, user_id, currency_id, balance, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (1, 1, 3, 50000.00000000, true, 0, CURRENT_TIMESTAMP, 'system-auto');
INSERT INTO crypto_wallet (id, user_id, currency_id, balance, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (2, 1, 1, 0.00000000, true, 0, CURRENT_TIMESTAMP, 'system-auto');
INSERT INTO crypto_wallet (id, user_id, currency_id, balance, ctl_act, ctl_tcn, ctl_cre_ts, ctl_cre_uid) VALUES (3, 1, 2, 0.00000000, true, 0, CURRENT_TIMESTAMP, 'system-auto');
