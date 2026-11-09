package com.mirae.hairlounge.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mirae.hairlounge.auth.service.LoginFailedException;
import com.mirae.hairlounge.auth.service.LoginService;
import com.mirae.hairlounge.common.security.AuthCookies;
import com.mirae.hairlounge.common.security.JwtTokenService;
import com.mirae.hairlounge.common.security.LoginUser;
import com.mirae.hairlounge.common.security.RedirectTargets;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 화면(S01)과 로그인 처리입니다(FR1.1, FR1.5). 로그아웃은 보안 필터 체인이 처리합니다.
 */
@Controller
public class LoginController {

    static final String EXPIRED_NOTICE = "로그인 시간이 지나 다시 로그인해 주세요.";
    static final String LOGOUT_NOTICE = "로그아웃했습니다.";

    private final LoginService loginService;
    private final JwtTokenService tokenService;
    private final AuthCookies authCookies;

    public LoginController(LoginService loginService, JwtTokenService tokenService, AuthCookies authCookies) {
        this.loginService = loginService;
        this.tokenService = tokenService;
        this.authCookies = authCookies;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String expired,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String redirect,
            Model model) {
        if (expired != null) {
            model.addAttribute("notice", EXPIRED_NOTICE);
        } else if (logout != null) {
            model.addAttribute("notice", LOGOUT_NOTICE);
        }
        return showForm(model, "", redirect);
    }

    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "") String loginId,
            @RequestParam(defaultValue = "") String password,
            @RequestParam(required = false) String redirect,
            Model model, HttpServletResponse response) {
        try {
            LoginUser user = loginService.login(loginId, password);
            authCookies.write(response, tokenService.issue(user.getId(), user.getRole()));
            return "redirect:" + RedirectTargets.safeOrDefault(redirect);
        } catch (LoginFailedException e) {
            model.addAttribute("error", e.getMessage());
            return showForm(model, loginId, redirect);
        }
    }

    /** 아이디 칸 값은 남기고 비밀번호 칸은 비웁니다(S01). 안전한 돌아갈 주소만 숨은 칸에 넣습니다(R-06). */
    private static String showForm(Model model, String loginId, String redirect) {
        model.addAttribute("loginId", loginId);
        model.addAttribute("redirect", RedirectTargets.isSafe(redirect) ? redirect : null);
        return "login";
    }
}
