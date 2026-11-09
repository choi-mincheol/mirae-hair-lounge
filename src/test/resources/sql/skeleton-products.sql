-- 상품 조회 테스트용 분류·상품·옵션. id는 자동 번호와 겹치지 않게 1000번대를 씁니다.
INSERT INTO category (id, name, created_at) VALUES (1001, '염색약', TIMESTAMP '2026-11-01 09:00:00');
INSERT INTO category (id, name, created_at) VALUES (1002, '샴푸', TIMESTAMP '2026-11-01 09:00:00');

-- 옵션 두 개가 있는 상품
INSERT INTO product (id, name, category_id, brand, base_price, base_cost, active, created_at, updated_at)
VALUES (1001, 'OO 염색약', 1001, 'OO', 9000, 6000, TRUE, TIMESTAMP '2026-11-01 09:00:00', TIMESTAMP '2026-11-01 09:00:00');
INSERT INTO product_option (id, product_id, name, barcode, price, cost, min_stock, current_stock, active, created_at, updated_at)
VALUES (1001, 1001, '7N', '8801000000071', 9000, 6000, 5, 2, TRUE, TIMESTAMP '2026-11-01 09:00:00', TIMESTAMP '2026-11-01 09:00:00');
INSERT INTO product_option (id, product_id, name, barcode, price, cost, min_stock, current_stock, active, created_at, updated_at)
VALUES (1002, 1001, '8N', NULL, 9500, 6000, 5, 15, FALSE, TIMESTAMP '2026-11-01 09:00:00', TIMESTAMP '2026-11-01 09:00:00');

-- 옵션이 없는 상품(정상 경로로는 생기지 않지만 조회는 안전해야 함)
INSERT INTO product (id, name, category_id, brand, base_price, base_cost, active, created_at, updated_at)
VALUES (1002, 'OO 샴푸', 1002, NULL, 15000, 9000, FALSE, TIMESTAMP '2026-11-01 09:00:00', TIMESTAMP '2026-11-01 09:00:00');
