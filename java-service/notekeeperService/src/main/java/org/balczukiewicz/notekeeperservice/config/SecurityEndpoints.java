package org.balczukiewicz.notekeeperservice.config;

import java.util.stream.Stream;


public class SecurityEndpoints {

    private SecurityEndpoints() {

    }

    public static final String[] AUTHENTICATION = {"/api/v1/auth/**"};

    public static final String[] DOCUMENTATION = {
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
            "/swagger-resources/**", "/webjars/**"
    };

    public static final String[] DEVELOPMENT = {
            "/h2-console", "/h2-console/**", "/actuator/**"
    };

    public static final String[] BROWSER = {"/favicon.ico", "/error"};

    public static final String[] ALL = Stream.of(AUTHENTICATION, DOCUMENTATION, DEVELOPMENT, BROWSER)
            .flatMap(Stream::of)
            .toArray(String[]::new);

}