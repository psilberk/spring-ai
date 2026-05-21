/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Oracle document loader.
 *
 * @author Spring AI Contributors
 */
@ConfigurationProperties(OracleDocumentLoaderProperties.CONFIG_PREFIX)
public class OracleDocumentLoaderProperties {

	public static final String CONFIG_PREFIX = "spring.ai.oracle.document-loader";

	private @Nullable String resource;

	@NestedConfigurationProperty
	private final OracleDocumentLoaderTableProperties table = new OracleDocumentLoaderTableProperties();

	@NestedConfigurationProperty
	private final OracleDocumentLoaderPreferencesProperties preferences = new OracleDocumentLoaderPreferencesProperties();

	public @Nullable String getResource() {
		return this.resource;
	}

	public void setResource(@Nullable String resource) {
		this.resource = resource;
	}

	public OracleDocumentLoaderTableProperties getTable() {
		return this.table;
	}

	public OracleDocumentLoaderPreferencesProperties getPreferences() {
		return this.preferences;
	}

}
