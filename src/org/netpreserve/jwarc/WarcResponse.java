/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2018 National Library of Australia and the jwarc contributors
 */

package org.netpreserve.jwarc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Optional;

public class WarcResponse extends WarcCaptureRecord {

    private HttpResponse http;

    WarcResponse(MessageVersion version, MessageHeaders headers, MessageBody body) {
        super(version, headers, body);
    }

    /**
     * Parses the HTTP response captured by this record.
     * <p>
     * This is a convenience method for <code>HttpResponse.parse(response.body().channel())</code>.
     */
    public HttpResponse http() throws IOException {
        if (http == null) {
            MessageBody body = body();
            if (body.position() != 0) throw new IllegalStateException("http() cannot be called after reading from body");
            if (body instanceof LengthedBody) {
                // if we can, save a copy of the raw header and push it back so we don't invalidate body
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                LengthedBody lengthed = (LengthedBody) body;
                http = HttpResponse.parse(lengthed.discardPushbackOnRead(), Channels.newChannel(baos));
                lengthed.pushback(baos.toByteArray());
            } else {
                http = HttpResponse.parse(body);
            }
        }
        return http;
    }

    @Override
    public MediaType payloadType() throws IOException {
        return http().contentType();
    }

    public Optional<WarcPayload> payload() throws IOException {
        if (contentType().base().equals(MediaType.HTTP)) {
            return Optional.of(new WarcPayload(http().body()) {

                @Override
                public MediaType type() {
                    return http.contentType();
                }

                @Override
                Optional<MediaType> identifiedType() {
                    return identifiedPayloadType();
                }

                @Override
                public Optional<WarcDigest> digest() {
                    return payloadDigest();
                }
            });
        }
        return Optional.empty();
    }

    public static class Builder extends AbstractBuilder<WarcResponse, Builder> {
        public Builder(URI targetURI) {
            this(targetURI.toString());
        }

        public Builder(String targetURI) {
            super("response");
            setHeader("WARC-Target-URI", targetURI);
        }

        public Builder body(HttpResponse httpResponse) throws IOException {
            return body(MediaType.HTTP_RESPONSE, httpResponse);
        }

        @Override
        public WarcResponse build() {
            return build(WarcResponse::new);
        }
    }
}
