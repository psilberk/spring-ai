/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import org.jspecify.annotations.Nullable;

import org.springframework.ai.oracle.embedding.OracleEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Oracle embedding.
 *
 * @author Spring AI Contributors
 */
@ConfigurationProperties(OracleEmbeddingProperties.CONFIG_PREFIX)
public class OracleEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.oracle.embedding";

	private boolean initializeOnStartup;

	private @Nullable String onnxDirectoryAlias;

	private @Nullable String onnxFile;

	private @Nullable String onnxModelName;

	private @Nullable String onnxCredential;

	private @Nullable String onnxUri;

	@NestedConfigurationProperty
	private final OracleEmbeddingOptions options = OracleEmbeddingOptions.builder().build();

	@NestedConfigurationProperty
	private final OracleEmbeddingPreferencesProperties preferences = new OracleEmbeddingPreferencesProperties();

	public OracleEmbeddingOptions getOptions() {
		return this.options;
	}

	public OracleEmbeddingPreferencesProperties getPreferences() {
		return this.preferences;
	}

	public boolean isInitializeOnStartup() {
		return this.initializeOnStartup;
	}

	public void setInitializeOnStartup(boolean initializeOnStartup) {
		this.initializeOnStartup = initializeOnStartup;
	}

	public @Nullable String getOnnxDirectoryAlias() {
		return this.onnxDirectoryAlias;
	}

	public void setOnnxDirectoryAlias(@Nullable String onnxDirectoryAlias) {
		this.onnxDirectoryAlias = onnxDirectoryAlias;
	}

	public @Nullable String getOnnxFile() {
		return this.onnxFile;
	}

	public void setOnnxFile(@Nullable String onnxFile) {
		this.onnxFile = onnxFile;
	}

	public @Nullable String getOnnxModelName() {
		return this.onnxModelName;
	}

	public void setOnnxModelName(@Nullable String onnxModelName) {
		this.onnxModelName = onnxModelName;
	}

	public @Nullable String getOnnxCredential() {
		return this.onnxCredential;
	}

	public void setOnnxCredential(@Nullable String onnxCredential) {
		this.onnxCredential = onnxCredential;
	}

	public @Nullable String getOnnxUri() {
		return this.onnxUri;
	}

	public void setOnnxUri(@Nullable String onnxUri) {
		this.onnxUri = onnxUri;
	}

}
