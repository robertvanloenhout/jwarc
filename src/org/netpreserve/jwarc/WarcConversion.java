/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2018 National Library of Australia and the jwarc contributors
 */

package org.netpreserve.jwarc;

import java.net.URI;
import java.util.Optional;

public class WarcConversion extends WarcTargetRecord {
    WarcConversion(ProtocolVersion version, Headers headers, WarcBodyChannel body) {
        super(version, headers, body);
    }

    /**
     * The record id of the source of the conversion.
     */
    public Optional<URI> refersTo() {
        return headers().sole("WARC-Refers-To").map(WarcRecord::parseRecordID);
    }

    public static class Builder extends WarcTargetRecord.Builder<WarcConversion, Builder> {
        public Builder() {
            super("conversion");
        }

        public Builder refersTo(URI recordId) {
            return addHeader("WARC-Refers-To", WarcRecord.formatId(recordId));
        }

        @Override
        public WarcConversion build() {
            return build(WarcConversion::new);
        }
    }
}