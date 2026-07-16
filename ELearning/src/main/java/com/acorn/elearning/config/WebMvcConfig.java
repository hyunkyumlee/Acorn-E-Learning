package com.acorn.elearning.config;

import com.acorn.elearning.security.AdminRequiredInterceptor;
import com.acorn.elearning.security.GuestOnlyInterceptor;
import com.acorn.elearning.security.LoginRequiredInterceptor;
import com.acorn.elearning.security.RememberMeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginRequiredInterceptor loginRequiredInterceptor;
    private final AdminRequiredInterceptor adminRequiredInterceptor;
    private final GuestOnlyInterceptor guestOnlyInterceptor;

    private final RememberMeInterceptor rememberMeInterceptor;

    public WebMvcConfig(LoginRequiredInterceptor loginRequiredInterceptor, AdminRequiredInterceptor adminRequiredInterceptor, GuestOnlyInterceptor guestOnlyInterceptor, RememberMeInterceptor rememberMeInterceptor) {
        this.loginRequiredInterceptor = loginRequiredInterceptor;
        this.adminRequiredInterceptor = adminRequiredInterceptor;
        this.guestOnlyInterceptor = guestOnlyInterceptor;
        this.rememberMeInterceptor = rememberMeInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // [추가] 다른 인터셉터보다 먼저 실행되도록 맨 위에 등록
        /// 진입 시 WelcomeController가 세션을 확인하기 전에 인터셉터가 쿠키로 세션을 복원하므로, /로 들어오면 자동으로 role별 홈으로 redirect된다.
        registry.addInterceptor(rememberMeInterceptor).addPathPatterns("/**");
        // [수정] 비밀번호 찾기/재설정 화면도 게스트 전용으로 추가 (로그인 상태면 자기 홈으로 redirect)
        registry.addInterceptor(guestOnlyInterceptor).addPathPatterns("/login", "/signup", "/password/forgot", "/password/reset");
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns(
                "/learning/**",
                "/practice/**",
                "/exams/**",
                "/analysis/**",
                "/api/practice/**",
                "/api/analyses",
                "/api/analyses/**",
                "/community/**",
                "/payments/**",
                "/mypage",
                "/ranking",
                "/settings/**");
        registry.addInterceptor(adminRequiredInterceptor).addPathPatterns("/admin/**");
    }
}
