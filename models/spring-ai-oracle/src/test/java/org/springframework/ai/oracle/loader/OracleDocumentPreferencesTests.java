/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.oracle.loader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link OracleDocumentPreferences}.
 *
 * @author Spring AI Contributors
 */
class OracleDocumentPreferencesTests {

	/**
	 * Verify all preference fields can be set and normalized.
	 */
	@Test
	void buildWithAllValues() {
		OracleDocumentPreferences preferences = OracleDocumentPreferences.builder()
			.plaintext(true)
			.charset("UTF8")
			.format("text")
			.build();

		assertThat(preferences.isEmpty()).isFalse();
		assertThat(preferences.getPlaintext()).isTrue();
		assertThat(preferences.getCharset()).isEqualTo("UTF8");
		assertThat(preferences.getFormat()).isEqualTo("TEXT");
		assertThat(preferences.toByteArray()).isNotEmpty();
	}

	/**
	 * Verify default preferences remain empty.
	 */
	@Test
	void emptyPreferencesRemainEmpty() {
		OracleDocumentPreferences preferences = OracleDocumentPreferences.builder().build();

		assertThat(preferences.isEmpty()).isTrue();
		assertThat(preferences.getPlaintext()).isNull();
		assertThat(preferences.getCharset()).isNull();
		assertThat(preferences.getFormat()).isNull();
	}

	/**
	 * Verify blank charset is rejected.
	 */
	@Test
	void rejectBlankCharset() {
		assertThatThrownBy(() -> OracleDocumentPreferences.builder().charset(" ").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("charset must not be empty");
	}

	/**
	 * Verify blank format is rejected.
	 */
	@Test
	void rejectBlankFormat() {
		assertThatThrownBy(() -> OracleDocumentPreferences.builder().format(" ").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("format must not be empty");
	}

	/**
	 * Verify unsupported format values are rejected.
	 */
	@Test
	void rejectUnsupportedFormat() {
		assertThatThrownBy(() -> OracleDocumentPreferences.builder().format("JSON").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("format must be one of: BINARY, TEXT, IGNORE");
	}

}
