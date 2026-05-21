/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * Configuration properties for Oracle table document source.
 *
 * @author Spring AI Contributors
 */
public class OracleDocumentLoaderTableProperties {

	private @Nullable String owner;

	private @Nullable String tableName;

	private @Nullable String columnName;

	public @Nullable String getOwner() {
		return this.owner;
	}

	public void setOwner(@Nullable String owner) {
		this.owner = owner;
	}

	public @Nullable String getTableName() {
		return this.tableName;
	}

	public void setTableName(@Nullable String tableName) {
		this.tableName = tableName;
	}

	public @Nullable String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(@Nullable String columnName) {
		this.columnName = columnName;
	}

	public boolean isConfigured() {
		return StringUtils.hasText(this.owner) || StringUtils.hasText(this.tableName)
				|| StringUtils.hasText(this.columnName);
	}

}
