package com.mirae.hairlounge.common.security;

/**
 * 로그인 후 돌아갈 주소를 검사합니다(R-06). 이 앱 안의 상대 경로만 받습니다.
 */
public final class RedirectTargets {

    public static final String DEFAULT_TARGET = "/";

    private RedirectTargets() {
    }

    /** "/"로 시작하고 "//"나 "/\"로 시작하지 않으며 역슬래시·제어 문자가 없는 경로만 돌려주고, 아니면 대시보드("/")입니다. */
    public static String safeOrDefault(String target) {
        return isSafe(target) ? target : DEFAULT_TARGET;
    }

    public static boolean isSafe(String target) {
        if (target == null || target.isEmpty() || target.charAt(0) != '/') {
            return false;
        }
        if (target.startsWith("//") || target.indexOf('\\') >= 0) {
            return false;
        }
        return target.chars().noneMatch(Character::isISOControl);
    }
}
