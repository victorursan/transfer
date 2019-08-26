package com.victor.bank.transfer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class PayloadSerde {
    private static final Logger log = LoggerFactory.getLogger(PayloadSerde.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new AfterburnerModule().setUseValueClassLoader(false));
        mapper.registerModule(new Jdk8Module());
    }

    public static <T> ByteBuffer serialize(final T object) {
        try {
            return ByteBuffer.wrap(mapper.writeValueAsBytes(object));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message, using raw value", e);
        }
        return ByteBuffer.wrap(object.toString().getBytes());
    }

    public static <T> T deserialize(final byte[] payload, final Class<T> clazz) throws IOException {
        return mapper.readValue(payload, clazz);
    }

    public static <T> T deserialize(final byte[] payload, final TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(payload, typeReference);
    }
}
