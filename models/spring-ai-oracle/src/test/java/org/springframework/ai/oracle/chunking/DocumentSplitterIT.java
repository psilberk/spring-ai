/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.oracle.chunking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import oracle.jdbc.OracleType;
import oracle.jdbc.provider.oson.OsonFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.oracle.OracleContainer;

import org.springframework.ai.document.Document;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Oracle {@link DocumentSplitter} with a real Oracle database.
 *
 * @author Spring AI Contributors
 */
class DocumentSplitterIT {

	private static final String UTL_TO_CHUNKS_SQL = "select json_value(t.column_value, '$.chunk_data' returning clob) "
			+ "as chunk_data from dbms_vector_chain.utl_to_chunks(?, ?) t";

	private static final String ORACLE_IMAGE_NAME = "gvenzl/oracle-free:23-faststart";

	private static final OsonFactory OSON_FACTORY = new OsonFactory();

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final OracleContainer oracleContainer = new OracleContainer(ORACLE_IMAGE_NAME)
		.withStartupTimeout(Duration.ofMinutes(5))
		.withStartupAttempts(3)
		.withSharedMemorySize(2L * 1024L * 1024L * 1024L);

	private static final String CONTENT = "The tower is 324 meters (1,063 ft) tall, "
			+ "about the same height as an 81-storey building, and the tallest "
			+ "structure in Paris. Its base is square, measuring 125 meters (410 ft) "
			+ "on each side. During its construction, the Eiffel Tower surpassed the "
			+ "Washington Monument to become the tallest man-made structure in the world, "
			+ "a title it held for 41 years until the Chrysler Building in New York City "
			+ "was finished in 1930. It was the first structure to reach a height of "
			+ "300 meters. Due to the addition of a broadcasting aerial at the top "
			+ "of the tower in 1957, it is now taller than the Chrysler Building "
			+ "by 5.2 meters (17 ft). Excluding transmitters, the Eiffel Tower is "
			+ "the second tallest free-standing structure in France after the " + "Millau Viaduct.";

	/**
	 * Verify character-based chunking returns multiple chunks.
	 */
	@Test
	@DisplayName("split string input by chars")
	void splitStringInputByChars() {
		assertUtlToChunksAvailable();
		DocumentSplitter splitter = DocumentSplitter.builder(dataSource())
			.preferences(OracleChunkingPreferences.builder().by("chars").max(50).build())
			.build();

		List<Document> chunks = splitter.split(new Document(CONTENT));

		assertThat(chunks.size()).isGreaterThan(1);
	}

	/**
	 * Verify default splitter construction returns non-empty chunks.
	 */
	@Test
	@DisplayName("split string input with default constructor")
	void splitStringInputWithDefaultConstructor() {
		assertUtlToChunksAvailable();
		DocumentSplitter splitter = new DocumentSplitter(dataSource());

		List<Document> chunks = splitter.split(new Document(CONTENT));

		assertThat(chunks).isNotEmpty();
		assertThat(chunks.stream().map(Document::getText).allMatch(text -> text != null && !text.isBlank())).isTrue();
	}

	/**
	 * Verify word-based chunking returns multiple chunks.
	 */
	@Test
	@DisplayName("split string input by words")
	void splitStringInputByWords() {
		assertUtlToChunksAvailable();
		DocumentSplitter splitter = DocumentSplitter.builder(dataSource())
			.preferences(OracleChunkingPreferences.builder().by("words").max(50).build())
			.build();

		List<Document> chunks = splitter.split(new Document(CONTENT));

		assertThat(chunks.size()).isGreaterThan(1);
		assertThat(chunks.stream().map(Document::getText)).allMatch(chunk -> !chunk.trim().startsWith("{"));
	}

	/**
	 * Verify document metadata is preserved while splitting.
	 */
	@Test
	@DisplayName("split doc input by chars")
	void splitDocInputByChars() {
		assertUtlToChunksAvailable();
		Document source = new Document(CONTENT, Map.of("a", 1, "b", 2));
		DocumentSplitter splitter = DocumentSplitter.builder(dataSource())
			.preferences(OracleChunkingPreferences.builder().by("chars").max(50).build())
			.build();

		List<Document> chunks = splitter.split(source);

		assertThat(chunks.size()).isGreaterThan(1);
		Document firstChunk = chunks.get(0);
		assertThat(firstChunk.getMetadata().get("a")).isEqualTo(1);
		assertThat(firstChunk.getMetadata().get("b")).isEqualTo(2);
	}

	/**
	 * Verify metadata is preserved when no explicit preferences are set.
	 */
	@Test
	@DisplayName("split doc input preserves metadata with null preferences")
	void splitDocInputWithNullPreferences() {
		assertUtlToChunksAvailable();
		Document source = new Document(CONTENT, Map.of("source", "it", "version", 1));
		DocumentSplitter splitter = new DocumentSplitter(dataSource());

		List<Document> chunks = splitter.split(source);

		assertThat(chunks).isNotEmpty();
		assertThat(chunks.get(0).getMetadata().get("source")).isEqualTo("it");
		assertThat(chunks.get(0).getMetadata().get("version")).isEqualTo(1);
	}

	/**
	 * Verify split mode and overlap preferences produce multiple chunks.
	 */
	@Test
	@DisplayName("split string input with split and overlap preference")
	void splitStringInputWithSplitAndOverlapPreference() {
		assertUtlToChunksAvailable();
		DocumentSplitter splitter = DocumentSplitter.builder(dataSource())
			.preferences(OracleChunkingPreferences.builder().by("words").max(50).split("sentence").build())
			.build();

		List<Document> chunks = splitter.split(new Document(CONTENT));

		assertThat(chunks.size()).isGreaterThan(1);
	}

	/**
	 * Verify empty input returns one empty chunk.
	 */
	@Test
	@DisplayName("split empty string input")
	void splitEmptyStringInput() {
		assertUtlToChunksAvailable();
		DocumentSplitter splitter = DocumentSplitter.builder(dataSource())
			.preferences(OracleChunkingPreferences.builder().by("words").max(50).build())
			.build();

		List<Document> chunks = splitter.split(new Document(""));

		assertThat(chunks).hasSize(1);
		assertThat(chunks.get(0).getText()).isEmpty();
	}

	/**
	 * Build a container-backed data source for tests.
	 * @return data source
	 */
	private DriverManagerDataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(oracleContainer().getJdbcUrl());
		dataSource.setUsername(oracleContainer().getUsername());
		dataSource.setPassword(oracleContainer().getPassword());
		return dataSource;
	}

	/**
	 * Assert that {@code UTL_TO_CHUNKS} is invokable in the test database.
	 */
	private void assertUtlToChunksAvailable() {
		try (Connection connection = dataSource().getConnection()) {
			boolean available = isUtlToChunksInvokable(connection);
			Assertions.assertTrue(available, "DBMS_VECTOR_CHAIN.UTL_TO_CHUNKS is not available.");
		}
		catch (SQLException ex) {
			Assertions.fail("Could not verify UTL_TO_CHUNKS availability.", ex);
		}
	}

	/**
	 * Start and return the shared Oracle test container.
	 * @return running Oracle container
	 */
	private static OracleContainer oracleContainer() {
		if (!oracleContainer.isRunning()) {
			oracleContainer.start();
		}
		return oracleContainer;
	}

	/**
	 * Check whether {@code UTL_TO_CHUNKS} can be called.
	 * @param connection JDBC connection
	 * @return {@code true} when invokable
	 * @throws SQLException if invocation fails unexpectedly
	 */
	private static boolean isUtlToChunksInvokable(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(UTL_TO_CHUNKS_SQL)) {
			Clob inputText = connection.createClob();
			inputText.setString(1, "chunk probe text");
			statement.setObject(1, inputText);
			statement.setObject(2, toOsonBytes(Map.of("by", "words", "max", 50)), OracleType.JSON);
			try (ResultSet ignored = statement.executeQuery()) {
				return true;
			}
		}
		catch (SQLException ex) {
			if (ex.getErrorCode() == 904 || ex.getErrorCode() == 6550) {
				return false;
			}
			throw ex;
		}
	}

	/**
	 * Serialize a value to Oracle OSON bytes.
	 * @param value value to serialize
	 * @return OSON bytes
	 */
	private static byte[] toOsonBytes(Object value) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (JsonGenerator generator = OSON_FACTORY.createGenerator(out)) {
				OBJECT_MAPPER.writeValue(generator, value);
				generator.flush();
			}
			return out.toByteArray();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to serialize chunking preferences to Oracle OSON", ex);
		}
	}

}
