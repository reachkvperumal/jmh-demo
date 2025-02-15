package com.kv.jmh.service;

import com.kv.jmh.dto.InfoDO;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class InfoService {

    private final WebClient webClient;

    @Value("${restful-api.path}")
    private String path;

    public InfoService(WebClient.Builder builder, @Value("${restful-api.endpoint}") String url) {
        System.out.println(url);
        webClient = builder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE)))
                .baseUrl(url)
                .build();
    }

    public List<InfoDO> getGadgetInfo() {
        CompletableFuture<List<InfoDO>> future = new CompletableFuture<>();
        webClient.get()
                .uri(path)
                .retrieve()
                .bodyToFlux(InfoDO.class)
                .collectList()
                .subscribe(v -> future.complete(v),
                        e -> future.completeExceptionally(ExceptionUtils.getRootCause(e)));
        try {
            return future.get();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            throw new RuntimeException(rootCause);
        }
    }
}
