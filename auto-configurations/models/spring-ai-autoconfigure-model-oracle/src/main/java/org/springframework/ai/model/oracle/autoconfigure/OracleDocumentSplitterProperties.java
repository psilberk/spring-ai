/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Oracle document splitter.
 *
 * @author Spring AI Contributors
 */
@ConfigurationProperties(OracleDocumentSplitterProperties.CONFIG_PREFIX)
public class OracleDocumentSplitterProperties {

	public static final String CONFIG_PREFIX = "spring.ai.oracle.document-splitter";

	@NestedConfigurationProperty
	private final OracleDocumentSplitterPreferencesProperties preferences = new OracleDocumentSplitterPreferencesProperties();

	public OracleDocumentSplitterPreferencesProperties getPreferences() {
		return this.preferences;
	}

}
