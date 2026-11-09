package com.mirae.hairlounge.report.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 로그인 후 첫 화면(S02)입니다. 뼈대 단계에서는 자리만 두고, 내용은 계획 Step 19에서 만듭니다.
 */
@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }
}
