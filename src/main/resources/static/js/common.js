/*
 * 모든 화면이 함께 쓰는 동작입니다.
 * 상태를 바꾸는 $.ajax 요청에는 화면의 meta 태그에 있는 CSRF 토큰을 X-XSRF-TOKEN 머리글로 붙입니다(ADR-005).
 * 화면별 스크립트보다 먼저 불러옵니다.
 */
(function ($) {
    'use strict';

    var token = $('meta[name="_csrf"]').attr('content');
    var headerName = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';
    var safeMethods = /^(GET|HEAD|OPTIONS|TRACE)$/i;

    if (!token) {
        return;
    }

    $.ajaxSetup({
        beforeSend: function (xhr, settings) {
            if (!safeMethods.test(settings.type)) {
                xhr.setRequestHeader(headerName, token);
            }
        }
    });
})(jQuery);
