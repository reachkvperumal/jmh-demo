package com.kv.jmh.service;

import com.kv.jmh.dto.InfoDO;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class InfoService {

    private final RestClient restClient;

    @Value("${restful-api.path}")
    private String path;

    public InfoService(WebClient.Builder builder,  @Value("${restful-api.endpoint}") String url) {
        System.out.println(url);
        restClient = RestClient.builder()
                .baseUrl(url)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public List<InfoDO> getGadgetInfo() {
        return restClient.get().uri(path)
                .retrieve()
                .onStatus(status -> status.value() >= 400,
                        (request, response) -> {
                            throw new RuntimeException("Failed to retrieve: " +
                                    response.getStatusText());
                        })
                .body(new ParameterizedTypeReference<List<InfoDO>>() {});
    }
}
