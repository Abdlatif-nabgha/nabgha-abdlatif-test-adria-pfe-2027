package com.bank.apigateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/accounts")
    public Mono<ResponseEntity<Map<String, String>>> accountFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "error", "Account Service is unavailable",
                        "message", "Le service des comptes est temporairement indisponible. Veuillez reessayer plus tard."
                )));
    }

    @RequestMapping("/transactions")
    public Mono<ResponseEntity<Map<String, String>>> transactionFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "error", "Transaction Service is unavailable",
                        "message", "Le service des transactions est temporairement indisponible. Veuillez reessayer plus tard."
                )));
    }
}

