package com.acorn.elearning.config;

import com.acorn.elearning.security.AdminRequiredInterceptor;
import com.acorn.elearning.security.GuestOnlyInterceptor;
import com.acorn.elearning.security.LoginRequiredInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginRequiredInterceptor loginRequiredInterceptor;
    private final AdminRequiredInterceptor adminRequiredInterceptor;
    private final GuestOnlyInterceptor guestOnlyInterceptor;
    public WebMvcConfig(LoginRequiredInterceptor loginRequiredInterceptor, AdminRequiredInterceptor adminRequiredInterceptor, GuestOnlyInterceptor guestOnlyInterceptor) {
        this.loginRequiredInterceptor = loginRequiredInterceptor;
        this.adminRequiredInterceptor = adminRequiredInterceptor;
        this.guestOnlyInterceptor = guestOnlyInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(guestOnlyInterceptor).addPathPatterns("/login", "/signup");
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/learning/**", "/exams/**", "/analysis/**", "/community/**", "/payments/**", "/mypage", "/ranking", "/settings/**");
        registry.addInterceptor(adminRequiredInterceptor).addPathPatterns("/admin/**");
    }
}
