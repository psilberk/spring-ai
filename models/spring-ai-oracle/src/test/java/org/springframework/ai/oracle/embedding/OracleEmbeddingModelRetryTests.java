/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package org.springframework.ai.oracle.embedding;

import java.sql.SQLException;
import java.sql.SQLTransientException;

import org.junit.jupiter.api.Test;

import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;
import org.springframework.jdbc.datasource.AbstractDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Retry-specific tests for {@link OracleEmbeddingModel}.
 *
 * @author Spring AI Contributors
 */
class OracleEmbeddingModelRetryTests {

	/**
	 * Verify transient SQL errors are retried and mapped to transient AI exception.
	 */
	@Test
	void embedTransientErrorIsRetriedAndPreservesExceptionType() {
		TestRetryListener retryListener = new TestRetryListener();
		RetryTemplate retryTemplate = shortRetryTemplate(retryListener);
		OracleEmbeddingModel model = new OracleEmbeddingModel(new AlwaysTransientFailingDataSource(), null, null,
				retryTemplate);

		assertThatThrownBy(() -> model.embed("hello")).isInstanceOf(TransientAiException.class)
			.hasMessage("Failed to generate Oracle embedding");
		assertThat(retryListener.onErrorRetryCount).isEqualTo(2);
	}

	/**
	 * Verify non-transient SQL errors are not retried.
	 */
	@Test
	void embedNonTransientErrorIsNotRetried() {
		TestRetryListener retryListener = new TestRetryListener();
		RetryTemplate retryTemplate = shortRetryTemplate(retryListener);
		OracleEmbeddingModel model = new OracleEmbeddingModel(new AlwaysNonTransientFailingDataSource(), null, null,
				retryTemplate);

		assertThatThrownBy(() -> model.embed("hello")).isInstanceOf(NonTransientAiException.class)
			.hasMessage("Failed to generate Oracle embedding");
		assertThat(retryListener.onErrorRetryCount).isZero();
	}

	/**
	 * Create a short retry template that retries transient AI exceptions.
	 * @param retryListener listener capturing retry attempts
	 * @return configured retry template
	 */
	private RetryTemplate shortRetryTemplate(TestRetryListener retryListener) {
		RetryPolicy retryPolicy = RetryPolicy.builder().maxRetries(2).includes(TransientAiException.class).build();
		RetryTemplate retryTemplate = new RetryTemplate(retryPolicy);
		retryTemplate.setRetryListener(retryListener);
		return retryTemplate;
	}

	private static final class AlwaysTransientFailingDataSource extends AbstractDataSource {

		/**
		 * Always throws transient SQL exception.
		 * @return never returns
		 * @throws SQLTransientException always thrown
		 */
		@Override
		public java.sql.Connection getConnection() throws SQLTransientException {
			throw new SQLTransientException("temporary db issue");
		}

		/**
		 * Always throws transient SQL exception.
		 * @param username ignored
		 * @param password ignored
		 * @return never returns
		 * @throws SQLTransientException always thrown
		 */
		@Override
		public java.sql.Connection getConnection(String username, String password) throws SQLTransientException {
			throw new SQLTransientException("temporary db issue");
		}

	}

	private static final class AlwaysNonTransientFailingDataSource extends AbstractDataSource {

		/**
		 * Always throws non-transient SQL exception.
		 * @return never returns
		 * @throws SQLException always thrown
		 */
		@Override
		public java.sql.Connection getConnection() throws SQLException {
			throw new SQLException("permanent db issue");
		}

		/**
		 * Always throws non-transient SQL exception.
		 * @param username ignored
		 * @param password ignored
		 * @return never returns
		 * @throws SQLException always thrown
		 */
		@Override
		public java.sql.Connection getConnection(String username, String password) throws SQLException {
			throw new SQLException("permanent db issue");
		}

	}

	private static final class TestRetryListener implements RetryListener {

		private int onErrorRetryCount;

		/**
		 * Increment retry counter on each retry callback.
		 * @param retryPolicy retry policy
		 * @param retryable retry descriptor
		 */
		@Override
		public void beforeRetry(RetryPolicy retryPolicy, Retryable<?> retryable) {
			this.onErrorRetryCount++;
		}

	}

}
