package com.neobankx.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    @RequestMapping(path = "/auth-service", method = {RequestMethod.GET, RequestMethod.POST})
    Mono<ProblemDetail> authServiceFallback() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Authentication service is temporarily unavailable"
        );
        problem.setTitle("AUTH_SERVICE_UNAVAILABLE");
        return Mono.just(problem);
    }
}
