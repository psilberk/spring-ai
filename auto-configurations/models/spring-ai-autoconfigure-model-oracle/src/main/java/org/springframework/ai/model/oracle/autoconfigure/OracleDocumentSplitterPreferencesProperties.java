/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for Oracle document splitter preferences.
 *
 * @author Spring AI Contributors
 */
public class OracleDocumentSplitterPreferencesProperties {

	private @Nullable String by;

	private @Nullable Integer max;

	private @Nullable Integer overlap;

	private @Nullable String split;

	private @Nullable List<String> customList;

	private @Nullable String vocabulary;

	private @Nullable String language;

	private @Nullable String normalize;

	private @Nullable List<String> normOptions;

	private @Nullable Boolean extended;

	public @Nullable String getBy() {
		return this.by;
	}

	public void setBy(@Nullable String by) {
		this.by = by;
	}

	public @Nullable Integer getMax() {
		return this.max;
	}

	public void setMax(@Nullable Integer max) {
		this.max = max;
	}

	public @Nullable Integer getOverlap() {
		return this.overlap;
	}

	public void setOverlap(@Nullable Integer overlap) {
		this.overlap = overlap;
	}

	public @Nullable String getSplit() {
		return this.split;
	}

	public void setSplit(@Nullable String split) {
		this.split = split;
	}

	public @Nullable List<String> getCustomList() {
		return this.customList;
	}

	public void setCustomList(@Nullable List<String> customList) {
		this.customList = customList;
	}

	public @Nullable String getVocabulary() {
		return this.vocabulary;
	}

	public void setVocabulary(@Nullable String vocabulary) {
		this.vocabulary = vocabulary;
	}

	public @Nullable String getLanguage() {
		return this.language;
	}

	public void setLanguage(@Nullable String language) {
		this.language = language;
	}

	public @Nullable String getNormalize() {
		return this.normalize;
	}

	public void setNormalize(@Nullable String normalize) {
		this.normalize = normalize;
	}

	public @Nullable List<String> getNormOptions() {
		return this.normOptions;
	}

	public void setNormOptions(@Nullable List<String> normOptions) {
		this.normOptions = normOptions;
	}

	public @Nullable Boolean getExtended() {
		return this.extended;
	}

	public void setExtended(@Nullable Boolean extended) {
		this.extended = extended;
	}

	public boolean isConfigured() {
		return StringUtils.hasText(this.by) || this.max != null || this.overlap != null
				|| StringUtils.hasText(this.split) || !CollectionUtils.isEmpty(this.customList)
				|| StringUtils.hasText(this.vocabulary) || StringUtils.hasText(this.language)
				|| StringUtils.hasText(this.normalize) || !CollectionUtils.isEmpty(this.normOptions)
				|| this.extended != null;
	}

}
