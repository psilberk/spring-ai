/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import javax.sql.DataSource;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.oracle.embedding.OracleEmbeddingModel;
import org.springframework.ai.oracle.embedding.OracleEmbeddingPreferences;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.StringUtils;

/**
 * {@link AutoConfiguration Auto-configuration} for Oracle Embedding Model.
 *
 * @author Spring AI Contributors
 */
@AutoConfiguration
@EnableConfigurationProperties(OracleEmbeddingProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.ORACLE,
		matchIfMissing = true)
@ConditionalOnClass({ OracleEmbeddingModel.class, DataSource.class })
public class OracleEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OracleEmbeddingModel oracleEmbeddingModel(DataSource dataSource, OracleEmbeddingProperties properties,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<EmbeddingModelObservationConvention> observationConvention,
			ObjectProvider<RetryTemplate> retryTemplate) {

		applyPreferences(properties);

		OracleEmbeddingModel embeddingModel = new OracleEmbeddingModel(dataSource, properties.getOptions(),
				observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP), retryTemplate.getIfUnique(),
				properties.isInitializeOnStartup(), properties.getOnnxDirectoryAlias(), properties.getOnnxFile(),
				properties.getOnnxModelName(), properties.getOnnxCredential(), properties.getOnnxUri());

		observationConvention.ifAvailable(embeddingModel::setObservationConvention);

		return embeddingModel;
	}

	private static void applyPreferences(OracleEmbeddingProperties properties) {
		OracleEmbeddingPreferencesProperties preferenceProperties = properties.getPreferences();
		if (!preferenceProperties.isConfigured()) {
			return;
		}

		OracleEmbeddingPreferences.Builder builder = OracleEmbeddingPreferences.builder()
			.provider(StringUtils.hasText(preferenceProperties.getProvider()) ? preferenceProperties.getProvider()
					: "database")
			.model(StringUtils.hasText(preferenceProperties.getModel()) ? preferenceProperties.getModel() : "database");

		if (StringUtils.hasText(preferenceProperties.getCredentialName())) {
			builder.credentialName(preferenceProperties.getCredentialName());
		}
		if (StringUtils.hasText(preferenceProperties.getUrl())) {
			builder.url(preferenceProperties.getUrl());
		}
		if (preferenceProperties.getTransferTimeout() != null) {
			builder.transferTimeout(preferenceProperties.getTransferTimeout());
		}
		if (preferenceProperties.getMaxCount() != null) {
			builder.maxCount(preferenceProperties.getMaxCount());
		}
		if (preferenceProperties.getBatchSize() != null) {
			builder.batchSize(preferenceProperties.getBatchSize());
		}

		properties.getOptions().setPreferences(builder.build());
	}

}
