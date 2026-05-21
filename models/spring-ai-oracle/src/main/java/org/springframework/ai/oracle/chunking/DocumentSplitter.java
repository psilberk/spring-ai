/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.oracle.chunking;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;
import org.jspecify.annotations.NullMarked;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.Assert;

/**
 * Oracle Database text splitter using DBMS_VECTOR_CHAIN.UTL_TO_CHUNKS.
 *
 * @author Spring AI Contributors
 */
public class DocumentSplitter extends TextSplitter {

	private static final String CHUNKING_SQL = "select json_value(t.column_value, '$.chunk_data' returning clob) "
			+ "as chunk_data from dbms_vector_chain.utl_to_chunks(?, ?) t";

	private final DataSource dataSource;

	private final byte[] preferencesOson;

	/**
	 * Create a splitter with default Oracle chunking behavior.
	 * @param dataSource Oracle data source
	 */
	public DocumentSplitter(DataSource dataSource) {
		this(dataSource, Builder.DEFAULT_PREFERENCES.toByteArray());
	}

	/**
	 * Internal constructor with optional OSON chunking preferences.
	 * @param dataSource Oracle data source
	 * @param preferencesOson Oracle JSON preferences bytes
	 */
	private DocumentSplitter(DataSource dataSource, byte[] preferencesOson) {
		Assert.notNull(dataSource, "dataSource must not be null");
		this.dataSource = dataSource;

		this.preferencesOson = preferencesOson;
	}

	/**
	 * Create a builder for splitter configuration.
	 * @return splitter builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a builder for splitter configuration.
	 * @param dataSource Oracle data source
	 * @return splitter builder
	 */
	public static Builder builder(DataSource dataSource) {
		return builder().dataSource(dataSource);
	}

	/**
	 * Create a builder for splitter configuration with explicit preferences.
	 * @param dataSource Oracle data source
	 * @param preferences chunking preferences
	 * @return splitter builder
	 */
	public static Builder builder(DataSource dataSource, OracleChunkingPreferences preferences) {
		return builder(dataSource).preferences(preferences);
	}

	/**
	 * Split text using {@code DBMS_VECTOR_CHAIN.UTL_TO_CHUNKS}.
	 * @param text source text
	 * @return chunk text values
	 */
	@Override
	@NullMarked
	protected List<String> splitText(String text) {
		Assert.notNull(text, "text must not be null");

		if (text.isEmpty()) {
			return List.of(text);
		}

		List<String> chunks = new ArrayList<>();

		try (Connection connection = this.dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(CHUNKING_SQL)) {

			statement.setString(1, text);
			if (this.preferencesOson == null) {
				statement.setNull(2, OracleTypes.JSON);
			}
			else {
				statement.setObject(2, this.preferencesOson, OracleTypes.JSON);
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					String chunkData = resultSet.getString("chunk_data");
					if (chunkData != null) {
						chunks.add(chunkData);
					}
				}
			}
		}
		catch (SQLException ex) {
			throw new IllegalStateException("Failed to split text using Oracle DBMS_VECTOR_CHAIN.UTL_TO_CHUNKS", ex);
		}

		return chunks.isEmpty() ? List.of(text) : chunks;
	}

	public static final class Builder {

		private static final OracleChunkingPreferences DEFAULT_PREFERENCES = OracleChunkingPreferences.builder()
			.by("words")
			.build();

		private DataSource dataSource;

		private OracleChunkingPreferences preferences = DEFAULT_PREFERENCES;

		/**
		 * Create an empty builder. Configure source and preferences before build.
		 */
		private Builder() {
		}

		/**
		 * Set Oracle data source.
		 * @param dataSource Oracle data source
		 * @return this builder
		 */
		public Builder dataSource(DataSource dataSource) {
			Assert.notNull(dataSource, "dataSource must not be null");
			this.dataSource = dataSource;
			return this;
		}

		/**
		 * Set structured chunking preferences.
		 * @param preferences chunking preferences
		 * @return this builder
		 */
		public Builder preferences(OracleChunkingPreferences preferences) {
			Assert.notNull(preferences, "preferences must not be null");
			this.preferences = preferences;
			return this;
		}

		/**
		 * Build a splitter from configured options.
		 * @return configured splitter
		 */
		public DocumentSplitter build() {
			Assert.notNull(this.dataSource, "dataSource must not be null");
			return new DocumentSplitter(this.dataSource, this.preferences.toByteArray());
		}

	}

}
