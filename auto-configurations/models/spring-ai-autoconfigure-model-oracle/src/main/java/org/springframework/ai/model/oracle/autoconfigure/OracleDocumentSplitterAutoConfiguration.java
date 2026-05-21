/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import javax.sql.DataSource;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.oracle.chunking.DocumentSplitter;
import org.springframework.ai.oracle.chunking.OracleChunkingPreferences;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * {@link AutoConfiguration Auto-configuration} for Oracle document splitter.
 *
 * @author Spring AI Contributors
 */
@AutoConfiguration
@EnableConfigurationProperties(OracleDocumentSplitterProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.ORACLE,
		matchIfMissing = true)
@ConditionalOnClass({ DocumentSplitter.class, DataSource.class })
public class OracleDocumentSplitterAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public DocumentSplitter oracleDocumentSplitter(DataSource dataSource, OracleDocumentSplitterProperties properties) {
		DocumentSplitter.Builder builder = DocumentSplitter.builder(dataSource);
		applyPreferences(builder, properties.getPreferences());
		return builder.build();
	}

	private static void applyPreferences(DocumentSplitter.Builder builder,
			OracleDocumentSplitterPreferencesProperties preferenceProperties) {
		if (!preferenceProperties.isConfigured()) {
			return;
		}

		OracleChunkingPreferences.Builder preferencesBuilder = OracleChunkingPreferences.builder();
		if (StringUtils.hasText(preferenceProperties.getBy())) {
			preferencesBuilder.by(preferenceProperties.getBy());
		}
		if (preferenceProperties.getMax() != null) {
			preferencesBuilder.max(preferenceProperties.getMax());
		}
		if (preferenceProperties.getOverlap() != null) {
			preferencesBuilder.overlap(preferenceProperties.getOverlap());
		}
		if (StringUtils.hasText(preferenceProperties.getSplit())) {
			preferencesBuilder.split(preferenceProperties.getSplit());
		}
		if (preferenceProperties.getCustomList() != null) {
			preferencesBuilder.customList(preferenceProperties.getCustomList());
		}
		if (StringUtils.hasText(preferenceProperties.getVocabulary())) {
			preferencesBuilder.vocabulary(preferenceProperties.getVocabulary());
		}
		if (StringUtils.hasText(preferenceProperties.getLanguage())) {
			preferencesBuilder.language(preferenceProperties.getLanguage());
		}
		if (StringUtils.hasText(preferenceProperties.getNormalize())) {
			preferencesBuilder.normalize(preferenceProperties.getNormalize());
		}
		if (preferenceProperties.getNormOptions() != null) {
			preferencesBuilder.normOptions(preferenceProperties.getNormOptions());
		}
		if (preferenceProperties.getExtended() != null) {
			preferencesBuilder.extended(preferenceProperties.getExtended());
		}

		builder.preferences(preferencesBuilder.build());
	}

}
