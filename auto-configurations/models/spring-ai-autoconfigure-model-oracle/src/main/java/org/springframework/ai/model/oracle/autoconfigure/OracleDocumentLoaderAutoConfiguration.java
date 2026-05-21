/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import javax.sql.DataSource;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.oracle.loader.OracleDocumentPreferences;
import org.springframework.ai.oracle.loader.OracleDocumentReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.StringUtils;

/**
 * {@link AutoConfiguration Auto-configuration} for Oracle document loader.
 *
 * @author Spring AI Contributors
 */
@AutoConfiguration
@EnableConfigurationProperties(OracleDocumentLoaderProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.ORACLE,
		matchIfMissing = true)
@ConditionalOnClass({ OracleDocumentReader.class, DataSource.class })
public class OracleDocumentLoaderAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = OracleDocumentLoaderProperties.CONFIG_PREFIX, name = "resource")
	public OracleDocumentReader oracleResourceDocumentLoader(DataSource dataSource,
			OracleDocumentLoaderProperties properties) {
		String resource = properties.getResource();
		if (resource == null) {
			throw new IllegalArgumentException(
					OracleDocumentLoaderProperties.CONFIG_PREFIX + ".resource must not be null");
		}
		OracleDocumentReader.Builder builder = OracleDocumentReader.builder(dataSource,
				new DefaultResourceLoader().getResource(resource));
		applyPreferences(builder, properties.getPreferences());
		return builder.build();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = OracleDocumentLoaderProperties.CONFIG_PREFIX + ".table",
			name = { "owner", "table-name", "column-name" })
	public OracleDocumentReader oracleTableDocumentLoader(DataSource dataSource,
			OracleDocumentLoaderProperties properties) {
		OracleDocumentLoaderTableProperties tableProperties = properties.getTable();
		OracleDocumentReader.Builder builder = OracleDocumentReader.builder(dataSource, tableProperties.getOwner(),
				tableProperties.getTableName(), tableProperties.getColumnName());
		applyPreferences(builder, properties.getPreferences());
		return builder.build();
	}

	private static void applyPreferences(OracleDocumentReader.Builder builder,
			OracleDocumentLoaderPreferencesProperties preferenceProperties) {
		if (!preferenceProperties.isConfigured()) {
			return;
		}

		OracleDocumentPreferences.Builder preferencesBuilder = OracleDocumentPreferences.builder();
		if (preferenceProperties.getPlaintext() != null) {
			preferencesBuilder.plaintext(preferenceProperties.getPlaintext());
		}
		if (StringUtils.hasText(preferenceProperties.getCharset())) {
			preferencesBuilder.charset(preferenceProperties.getCharset());
		}
		if (StringUtils.hasText(preferenceProperties.getFormat())) {
			preferencesBuilder.format(preferenceProperties.getFormat());
		}
		builder.preferences(preferencesBuilder.build());
	}

}
