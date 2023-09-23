package com.example.api.users.shared;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(FeignErrorDecoder.class);

    private final Environment environment;

    public FeignErrorDecoder(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        return switch (response.status()) {
            case 400 ->
                // Do something
                    new BadRequestException(response.reason());
            case 404 -> {
                // Do something
                LOG.info("method key " + methodKey);

                if (methodKey.contains("getAlbums")) {
                    yield new ResponseStatusException(
                            HttpStatusCode.valueOf(response.status()),
                            environment.getProperty("albums.exceptions.albums-not-found"));
                } else {
                    yield new ResponseStatusException(
                            HttpStatusCode.valueOf(response.status()),
                            response.reason());
                }
            }
            default -> new Exception(response.reason());
        };

    }
}
