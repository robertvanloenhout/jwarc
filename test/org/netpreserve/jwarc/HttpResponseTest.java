package org.netpreserve.jwarc;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertEquals;

public class HttpResponseTest {
    @Test
    public void serializeHeaderShouldPreserveExactly() throws IOException {
        String header = "HTTP/1.0 404 Not Found\r\n" +
                "Server:  example\n" +
                "Content-Length: 6\r\n\r\n";
        String message = header + "[body]";
        HttpResponse response = HttpResponse.parse(Channels.newChannel(new ByteArrayInputStream(message.getBytes(US_ASCII))));
        assertEquals(404, response.status());
        assertEquals("Not Found", response.reason());
        assertEquals(header, new String(response.serializeHeader(), US_ASCII));
    }
}