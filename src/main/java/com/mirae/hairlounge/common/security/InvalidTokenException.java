package com.mirae.hairlounge.common.security;

/**
 * 만료되었거나 서명이 틀린 토큰입니다. 토큰 값은 메시지에 넣지 않습니다(NFR3).
 */
public class InvalidTokenException extends RuntimeException {

    private final boolean expired;

    public InvalidTokenException(boolean expired, Throwable cause) {
        super(expired ? "토큰이 만료되었습니다." : "토큰이 올바르지 않습니다.", cause);
        this.expired = expired;
    }

    public boolean isExpired() {
        return expired;
    }
}
