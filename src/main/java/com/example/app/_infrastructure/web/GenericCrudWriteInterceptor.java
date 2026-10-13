package com.example.app._infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rejects every write request that would reach one of Spring Data REST's generic CRUD controllers with
 * {@code 405 Method Not Allowed}, before the controller method's arguments are resolved.
 *
 * <p>
 * {@link WebConfiguration} disables the write methods through Spring Data REST's exposure configuration, but Spring
 * Data REST checks it only inside the controller method, after it has bound the request body to the entity. For
 * {@code PUT} and {@code PATCH} on an item resource that binding fails first on an aggregate without setters - with
 * a {@code 400}, or with a {@code 500} when it tries to instantiate the aggregate - so the lockdown would hold only
 * by accident, and stop holding once an aggregate gains a setter. The operations controllers of the features are
 * not affected: they are application beans, not part of Spring Data REST.
 */
class GenericCrudWriteInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final List<String> READ_METHODS = List.of("GET", "HEAD", "OPTIONS");
    // the package of Spring Data REST's own (package-private) controllers; a feature's operations controller carries
    // the @RepositoryRestController annotation from there, but lives in the feature's package
    private static final String SPRING_DATA_REST_CONTROLLERS = RepositoryRestController.class.getPackageName();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws HttpRequestMethodNotSupportedException {
        if (handler instanceof HandlerMethod handlerMethod
                && handlerMethod.getBeanType().getPackageName().equals(SPRING_DATA_REST_CONTROLLERS)
                && WRITE_METHODS.contains(request.getMethod())) {
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), READ_METHODS);
        }
        return true;
    }
}
