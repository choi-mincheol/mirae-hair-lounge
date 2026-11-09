package com.mirae.hairlounge.auth.service;

/**
 * 로그인 실패입니다. 틀린 비밀번호, 없는 아이디, 사용 중지 계정 모두 같은 문구를 씁니다(FR1.1, FR1.3).
 */
public class LoginFailedException extends RuntimeException {

    public static final String MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다";

    public LoginFailedException() {
        super(MESSAGE);
    }
}
