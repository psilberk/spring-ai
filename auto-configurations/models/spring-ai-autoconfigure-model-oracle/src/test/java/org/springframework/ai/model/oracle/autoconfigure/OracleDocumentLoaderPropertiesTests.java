/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.model.oracle.autoconfigure;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OracleDocumentLoaderProperties}.
 *
 * @author Spring AI Contributors
 */
public class OracleDocumentLoaderPropertiesTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withBean(DataSource.class,
			() -> Mockito.mock(DataSource.class));

	@Test
	public void loaderProperties() {
		this.contextRunner.withPropertyValues(
		// @formatter:off
				"spring.ai.oracle.document-loader.resource=classpath:/docs",
				"spring.ai.oracle.document-loader.table.owner=APP",
				"spring.ai.oracle.document-loader.table.table-name=DOCS",
				"spring.ai.oracle.document-loader.table.column-name=TEXT",
				"spring.ai.oracle.document-loader.preferences.plaintext=true",
				"spring.ai.oracle.document-loader.preferences.charset=UTF-8",
				"spring.ai.oracle.document-loader.preferences.format=ignore")
				// @formatter:on
			.withConfiguration(AutoConfigurations.of(OracleDocumentLoaderAutoConfiguration.class))
			.run(context -> {
				OracleDocumentLoaderProperties properties = context.getBean(OracleDocumentLoaderProperties.class);

				assertThat(properties.getResource()).isEqualTo("classpath:/docs");
				assertThat(properties.getTable().getOwner()).isEqualTo("APP");
				assertThat(properties.getTable().getTableName()).isEqualTo("DOCS");
				assertThat(properties.getTable().getColumnName()).isEqualTo("TEXT");
				assertThat(properties.getPreferences().getPlaintext()).isTrue();
				assertThat(properties.getPreferences().getCharset()).isEqualTo("UTF-8");
				assertThat(properties.getPreferences().getFormat()).isEqualTo("ignore");
			});
	}

}
