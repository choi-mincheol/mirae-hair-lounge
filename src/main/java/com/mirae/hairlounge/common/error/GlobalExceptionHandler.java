package com.mirae.hairlounge.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 공통 오류 처리(S20). 화면에는 오류 코드나 기술 내용을 보여주지 않습니다(NFR3).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class})
    public ModelAndView handleNotFound() {
        return errorView("error/404", HttpStatus.NOT_FOUND);
    }

    /** 서비스·컨트롤러에서 권한 검사로 거부된 요청입니다(FR1.4). */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied() {
        return errorView("error/403", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception exception) {
        log.error("요청을 처리하지 못했습니다.", exception);
        return errorView("error/500", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ModelAndView errorView(String viewName, HttpStatus status) {
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        return modelAndView;
    }
}
