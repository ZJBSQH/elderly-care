package com.elderlycare.common.security.config;

import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * 安全配置基类，各服务继承并自定义 permitAll 路径
 */
@EnableWebSecurity
public abstract class BaseSecurityConfig {

    protected abstract JwtAuthenticationFilter jwtAuthenticationFilter();

    /**
     * 子类覆盖此方法定义需要放行的路径
     */
    protected String[] permitAllPaths() {
        return new String[]{"/auth/**", "/ws/**"};
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    String[] paths = permitAllPaths();
                    if (paths.length > 0) {
                        auth.requestMatchers(paths).permitAll();
                    }
                    auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
                            .anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
