package com.mirae.hairlounge.common.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 요청마다 JWT 쿠키를 검사합니다(FR1.1, FR1.5, ADR-006).
 * <ul>
 * <li>검증을 통과하고 계정이 사용 중이면 로그인 상태로 둡니다(ADR-002).</li>
 * <li>발급된 지 5분이 지난 토큰이면 새 토큰으로 쿠키를 바꿉니다.</li>
 * <li>만료·서명 틀림·사용 중지 계정이면 쿠키를 지우고 표시만 남깁니다. 로그인 화면으로 보내는 일은
 * {@link LoginRedirectEntryPoint}가 합니다.</li>
 * </ul>
 * 빈으로 등록하지 않고 SecurityConfig에서 만들어 보안 필터 체인에만 넣습니다(서블릿 필터로 두 번 등록되지 않게).
 */
public class JwtCookieFilter extends OncePerRequestFilter {

    public static final String TOKEN_REJECTED_ATTRIBUTE = JwtCookieFilter.class.getName() + ".TOKEN_REJECTED";

    private static final List<String> STATIC_PREFIXES = List.of("/webjars/", "/css/", "/js/", "/favicon.ico");

    private final JwtTokenService tokenService;
    private final UserAccountLookup userAccountLookup;
    private final AuthCookies authCookies;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public JwtCookieFilter(JwtTokenService tokenService, UserAccountLookup userAccountLookup,
            AuthCookies authCookies, SecurityContextRepository securityContextRepository) {
        this.tokenService = tokenService;
        this.userAccountLookup = userAccountLookup;
        this.authCookies = authCookies;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return STATIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        authCookies.read(request).ifPresent(token -> authenticate(token, request, response));
        chain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request, HttpServletResponse response) {
        TokenClaims claims;
        try {
            claims = tokenService.parse(token);
        } catch (InvalidTokenException e) {
            reject(request, response);
            return;
        }
        Optional<AccountStatus> account = userAccountLookup.findById(claims.userId()).filter(AccountStatus::enabled);
        if (account.isEmpty()) {
            reject(request, response);
            return;
        }
        LoginUser user = LoginUser.from(account.get());
        storeAuthentication(user, request, response);
        if (tokenService.shouldReissue(claims)) {
            authCookies.write(response, tokenService.issue(user.getId(), user.getRole()));
        }
    }

    private void storeAuthentication(LoginUser user, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of(new SimpleGrantedAuthority(user.getRole().authority())));
        SecurityContext context = contextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute(TOKEN_REJECTED_ATTRIBUTE, Boolean.TRUE);
        authCookies.clear(response);
    }
}
