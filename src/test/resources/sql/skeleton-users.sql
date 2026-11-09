-- 매퍼 테스트용 사용자. password_hash는 자리만 채운 값이라 이 계정으로는 로그인할 수 없습니다.
-- 로그인이 필요한 테스트는 TestUsers 도우미가 PasswordEncoder로 해시를 만들어 넣습니다.
INSERT INTO user_account (id, login_id, name, password_hash, role, enabled, created_at)
VALUES (1001, 'admin01', '홍길동', 'not-a-real-hash', 'ADMIN', TRUE, TIMESTAMP '2026-11-01 09:00:00');
INSERT INTO user_account (id, login_id, name, password_hash, role, enabled, created_at)
VALUES (1002, 'staff01', '김직원', 'not-a-real-hash', 'STAFF', TRUE, TIMESTAMP '2026-11-01 09:00:00');
INSERT INTO user_account (id, login_id, name, password_hash, role, enabled, created_at)
VALUES (1003, 'staff02', '이중지', 'not-a-real-hash', 'STAFF', FALSE, TIMESTAMP '2026-11-01 09:00:00');
