INSERT INTO crypto_user (id, username, ctl_act, ctl_tcn, ctl_cre_ts, ctl_mod_ts, ctl_cre_uid, ctl_mod_uid) VALUES (1, 'demo', true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-auto', 'system-auto');

INSERT INTO crypto_wallet (user_id, currency, balance, ctl_act, ctl_tcn, ctl_cre_ts, ctl_mod_ts, ctl_cre_uid, ctl_mod_uid) VALUES (1, 'USDT', 50000.00000000, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-auto', 'system-auto');
INSERT INTO crypto_wallet (user_id, currency, balance, ctl_act, ctl_tcn, ctl_cre_ts, ctl_mod_ts, ctl_cre_uid, ctl_mod_uid) VALUES (1, 'BTC', 0.00000000, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-auto', 'system-auto');
INSERT INTO crypto_wallet (user_id, currency, balance, ctl_act, ctl_tcn, ctl_cre_ts, ctl_mod_ts, ctl_cre_uid, ctl_mod_uid) VALUES (1, 'ETH', 0.00000000, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-auto', 'system-auto');
