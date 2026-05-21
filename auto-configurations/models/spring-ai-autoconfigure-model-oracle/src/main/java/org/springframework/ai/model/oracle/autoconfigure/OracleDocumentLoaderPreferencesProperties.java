/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * Configuration properties for Oracle document loader preferences.
 *
 * @author Spring AI Contributors
 */
public class OracleDocumentLoaderPreferencesProperties {

	private @Nullable Boolean plaintext;

	private @Nullable String charset;

	private @Nullable String format;

	public @Nullable Boolean getPlaintext() {
		return this.plaintext;
	}

	public void setPlaintext(@Nullable Boolean plaintext) {
		this.plaintext = plaintext;
	}

	public @Nullable String getCharset() {
		return this.charset;
	}

	public void setCharset(@Nullable String charset) {
		this.charset = charset;
	}

	public @Nullable String getFormat() {
		return this.format;
	}

	public void setFormat(@Nullable String format) {
		this.format = format;
	}

	public boolean isConfigured() {
		return this.plaintext != null || StringUtils.hasText(this.charset) || StringUtils.hasText(this.format);
	}

}
