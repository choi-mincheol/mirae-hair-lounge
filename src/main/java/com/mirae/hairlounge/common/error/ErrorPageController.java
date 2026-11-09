package com.mirae.hairlounge.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 보안 필터가 거부한 요청(권한 없음, CSRF 토큰 없음)을 "권한이 없습니다" 화면(S20)으로 보여줍니다.
 */
@Controller
public class ErrorPageController {

    @RequestMapping("/denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String denied() {
        return "error/403";
    }
}
