package com.victor.bank.transfer.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.victor.bank.transfer.models.requests.AddClientReq;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import static com.victor.bank.transfer.utils.PayloadSerde.deserialize;
import static com.victor.bank.transfer.utils.PayloadSerde.serialize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class PayloadSerdeTest {

    @Test
    void testSerialize() {
        var testString = "someString";
        var jsonString = String.format("\"%s\"", testString);
        var responseBytes = serialize(testString);
        assertThat(responseBytes, is(ByteBuffer.wrap(jsonString.getBytes())));
    }

    @Test
    void testDeserialize() {
        var testString = "someString";
        var jsonString = String.format("\"%s\"", testString);
        try {
            var response = deserialize(jsonString.getBytes(), String.class);
            assertThat(response, is(testString));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testDeserialize2() {
        var testString = "someString";
        var jsonString = String.format("\"%s\"", testString);
        try {
            var response = deserialize(jsonString.getBytes(), new TypeReference<String>() {
            });
            assertThat(response, is(testString));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testDeserialize3() {
        var testString = "{\n" +
            "\t\"fullName\": \"Victor Ursan\"\t\n" +
            "}";
        try {
            AddClientReq response = deserialize(testString.getBytes(), AddClientReq.class);
            assertThat(response, is(new AddClientReq("Victor Ursan", Optional.empty())));
        } catch (IOException e) {
            fail(e);
        }
    }
}