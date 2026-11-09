-- dev 프로필에서만 넣는 예시 데이터입니다. 이미 있으면 넣지 않습니다.
-- H2와 MariaDB 모두에서 도는 INSERT ... SELECT ... WHERE NOT EXISTS 문법을 씁니다.

INSERT INTO category (name, created_at)
SELECT '염색약', CURRENT_TIMESTAMP
FROM (SELECT 1 AS one) seed
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '염색약');

INSERT INTO product (name, category_id, brand, base_price, base_cost, active, created_at, updated_at)
SELECT '예시 염색약', c.id, '예시', 9000, 6000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM category c
WHERE c.name = '염색약'
  AND NOT EXISTS (SELECT 1 FROM product WHERE name = '예시 염색약');

INSERT INTO product_option (product_id, name, barcode, price, cost, min_stock, current_stock, active, created_at, updated_at)
SELECT p.id, '7N', NULL, 9000, 6000, 5, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM product p
WHERE p.name = '예시 염색약'
  AND NOT EXISTS (SELECT 1 FROM product_option o WHERE o.product_id = p.id AND o.name = '7N');

INSERT INTO product_option (product_id, name, barcode, price, cost, min_stock, current_stock, active, created_at, updated_at)
SELECT p.id, '8N', NULL, 9000, 6000, 5, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM product p
WHERE p.name = '예시 염색약'
  AND NOT EXISTS (SELECT 1 FROM product_option o WHERE o.product_id = p.id AND o.name = '8N');
