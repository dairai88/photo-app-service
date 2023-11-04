package com.example.api.users.data;

import com.example.api.users.ui.model.AlbumResponseModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "albums-ws")
public interface AlbumsServiceClient {

    Logger LOG = LoggerFactory.getLogger(AlbumsServiceClient.class);

    @GetMapping("/users/{id}/albums")
    @Retry(name = "albums-ws")
    @CircuitBreaker(name = "albums-ws", fallbackMethod = "getAlbumsFallback")
    List<AlbumResponseModel> getAlbums(@PathVariable("id") String id, 
    		@RequestHeader("Authorization") String authorization);

    default List<AlbumResponseModel> getAlbumsFallback(String id, String authorization, Throwable exception) {
        LOG.error("Parameter {}", id);
        LOG.error("authorization {}", authorization);
        LOG.error("exception {}", exception.getLocalizedMessage());
        return List.of();
    }
}
