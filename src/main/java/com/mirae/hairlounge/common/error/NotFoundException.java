package com.mirae.hairlounge.common.error;

/**
 * 요청한 자료가 없을 때 던집니다. 공통 오류 처리가 "찾을 수 없음" 화면(S20)으로 바꿉니다.
 */
public class NotFoundException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "요청한 화면이나 자료를 찾을 수 없습니다.";

    public NotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
