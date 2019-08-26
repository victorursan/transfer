package com.victor.bank.transfer.utils;

import com.victor.bank.transfer.models.response.ErrorResp;
import io.undertow.io.Receiver;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.victor.bank.transfer.utils.PayloadSerde.deserialize;
import static com.victor.bank.transfer.utils.PayloadSerde.serialize;

public final class UndertowUtils {
    private static final Logger log = LoggerFactory.getLogger(UndertowUtils.class);

    public static void runAsync(final HttpServerExchange exchange, final Consumer<HttpServerExchange> handler) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(() -> handler.accept(exchange));
        } else {
            handler.accept(exchange);
        }
    }

    public static <T> void respondJson(final HttpServerExchange exchange, final int statusCode, final T responseObject) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(statusCode).getResponseSender().send(serialize(responseObject));
    }


    public static void getErrorCallback(final HttpServerExchange exchange, final IOException err) {
        log.error("Received unexpected error callback.", err);
        respondJson(exchange, StatusCodes.INTERNAL_SERVER_ERROR, ErrorResp.of(err.getMessage()));
    }

    @NotNull
    public static <T> BiConsumer<T, Throwable> createResponse(final HttpServerExchange exchange, final int statusCode) {
        return (response, err) -> {
            if (err != null) {
                log.error(err.getMessage(), err);
                respondJson(exchange, StatusCodes.BAD_REQUEST, ErrorResp.of(err.getMessage()));
            } else {
                respondJson(exchange, statusCode, response);
            }
        };
    }


    public static <T, U> Receiver.FullBytesCallback validateAndRunAsync(final Class<T> clazz,
                                                                        final Function<T, List<String>> validation,
                                                                        final Function<T, CompletableFuture<U>> toRun,
                                                                        final int statusCode) {
        return (HttpServerExchange exchange, byte[] payload) -> {
            try {
                final T message = deserialize(payload, clazz);
                final List<String> errors = validation.apply(message);
                if (errors.isEmpty()) {
                    runAsync(exchange, buildResponse(toRun.apply(message), statusCode));
                } else {
                    respondJson(exchange, StatusCodes.BAD_REQUEST, ErrorResp.of(errors.toString()));
                }
            } catch (Exception e) {
                log.error("Failed to deserialize payload.", e);
                respondJson(exchange, StatusCodes.BAD_REQUEST, ErrorResp.of(e.getMessage()));
            }
        };
    }

    public static <U> Consumer<HttpServerExchange> buildResponse(final CompletableFuture<U> future, final int statusCode) {
        return exchange -> future.whenComplete(createResponse(exchange, statusCode));
    }

}
