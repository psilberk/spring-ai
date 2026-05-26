/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.model.oracle.autoconfigure;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * Bindable Oracle embedding preferences properties.
 *
 * @author Spring AI Contributors
 */
public class OracleEmbeddingPreferencesProperties {

	private @Nullable String provider;

	private @Nullable String model;

	private @Nullable String credentialName;

	private @Nullable String url;

	private @Nullable Integer transferTimeout;

	private @Nullable Integer maxCount;

	private @Nullable Integer batchSize;

	public @Nullable String getProvider() {
		return this.provider;
	}

	public void setProvider(@Nullable String provider) {
		this.provider = provider;
	}

	public @Nullable String getModel() {
		return this.model;
	}

	public void setModel(@Nullable String model) {
		this.model = model;
	}

	public @Nullable String getCredentialName() {
		return this.credentialName;
	}

	public void setCredentialName(@Nullable String credentialName) {
		this.credentialName = credentialName;
	}

	public @Nullable String getUrl() {
		return this.url;
	}

	public void setUrl(@Nullable String url) {
		this.url = url;
	}

	public @Nullable Integer getTransferTimeout() {
		return this.transferTimeout;
	}

	public void setTransferTimeout(@Nullable Integer transferTimeout) {
		this.transferTimeout = transferTimeout;
	}

	public @Nullable Integer getMaxCount() {
		return this.maxCount;
	}

	public void setMaxCount(@Nullable Integer maxCount) {
		this.maxCount = maxCount;
	}

	public @Nullable Integer getBatchSize() {
		return this.batchSize;
	}

	public void setBatchSize(@Nullable Integer batchSize) {
		this.batchSize = batchSize;
	}

	boolean isConfigured() {
		return StringUtils.hasText(this.provider) || StringUtils.hasText(this.model)
				|| StringUtils.hasText(this.credentialName) || StringUtils.hasText(this.url)
				|| this.transferTimeout != null || this.maxCount != null || this.batchSize != null;
	}

}
