# Spring AI Oracle Embedding Auto-Configuration

This module provides Spring Boot auto-configuration for the Oracle embedding model (`OracleEmbeddingModel`).

Artifact:
- `org.springframework.ai:spring-ai-autoconfigure-model-oracle`

## What This Module Does

When enabled, this module creates and configures an `OracleEmbeddingModel` bean using:
- your `DataSource`
- `spring.ai.oracle.embedding.*` properties
- optional observation and retry beans (if present)

It also maps nested preference properties into Oracle OSON preferences used by `DBMS_VECTOR_CHAIN`.

## When Auto-Configuration Is Active

Auto-configuration is active when all are true:
1. `OracleEmbeddingModel` and `DataSource` are on the classpath.
2. A `DataSource` bean exists.
3. No custom `OracleEmbeddingModel` bean is already defined.
4. `spring.ai.model.embedding=oracle` (or missing, because this auto-config has `matchIfMissing=true`).

Recommended explicit setting:

```yaml
spring:
  ai:
    model:
      embedding: oracle
```

## Configuration Properties

Prefix: `spring.ai.oracle.embedding`

### Top-Level Properties

| Property | Type | Description |
|---|---|---|
| `spring.ai.oracle.embedding.initialize-on-startup` | `boolean` | If `true`, loads an ONNX model during bean initialization. |
| `spring.ai.oracle.embedding.onnx-directory-alias` | `String` | Oracle directory alias for local ONNX loading. |
| `spring.ai.oracle.embedding.onnx-file` | `String` | ONNX file name in the Oracle directory alias. |
| `spring.ai.oracle.embedding.onnx-model-name` | `String` | Model name used when loading ONNX. Falls back to `options.model` when not set. |
| `spring.ai.oracle.embedding.onnx-credential` | `String` | Oracle credential name for cloud ONNX loading. |
| `spring.ai.oracle.embedding.onnx-uri` | `String` | Cloud object URI for ONNX file. |

Important:
- Use one ONNX strategy only:
  - local: `onnx-directory-alias` + `onnx-file`
  - cloud: `onnx-credential` + `onnx-uri`
- Setting both local and cloud config causes startup failure.

### Options Properties

| Property | Type | Description |
|---|---|---|
| `spring.ai.oracle.embedding.options.model` | `String` | Embedding model identifier. Default is `"database"`. |
| `spring.ai.oracle.embedding.options.dimensions` | `Integer` | Requested embedding dimensions. |
| `spring.ai.oracle.embedding.options.proxy` | `String` | Optional outbound HTTP proxy used by Oracle calls. |
| `spring.ai.oracle.embedding.options.batching` | `boolean` | Enables batched embedding requests. Default is `true`. |
| `spring.ai.oracle.embedding.options.metadata-mode` | `MetadataMode` | Controls document metadata included in embedding content. Default is `EMBED`. |

### Preferences Properties

These map to Oracle embedding preferences passed as OSON.

| Property | Type | Description |
|---|---|---|
| `spring.ai.oracle.embedding.preferences.provider` | `String` | Provider name (for example `database`, `ocigenai`). |
| `spring.ai.oracle.embedding.preferences.model` | `String` | Provider-specific model name. |
| `spring.ai.oracle.embedding.preferences.credential-name` | `String` | Oracle credential name. |
| `spring.ai.oracle.embedding.preferences.url` | `String` | Provider endpoint URL. |
| `spring.ai.oracle.embedding.preferences.transfer-timeout` | `Integer` | Transfer timeout value. |
| `spring.ai.oracle.embedding.preferences.max-count` | `Integer` | Maximum input count. |
| `spring.ai.oracle.embedding.preferences.batch-size` | `Integer` | Batch size. |

Notes:
- If preferences are not configured, defaults are used (`provider=database`, `model=database`).
- If preferences are configured partially, unspecified provider/model fields default to `database`.

## Example Configurations

### 1) Oracle Database Provider (local database embeddings)

```yaml
spring:
  ai:
    model:
      embedding: oracle
    oracle:
      embedding:
        options:
          model: ALL_MINILM_L12_V2
          batching: true
        preferences:
          provider: database
          model: ALL_MINILM_L12_V2
```

### 2) OCI GenAI-Style Preferences

```yaml
spring:
  ai:
    model:
      embedding: oracle
    oracle:
      embedding:
        options:
          model: e5-small
          dimensions: 384
          metadata-mode: all
        preferences:
          provider: ocigenai
          model: cohere.embed-english-light-v3.0
          credential-name: OCI_CRED
          url: https://inference.generativeai.us-chicago-1.oci.oraclecloud.com
          transfer-timeout: 30
          max-count: 100
          batch-size: 8
```

### 3) Initialize ONNX Model on Startup (Local File)

```yaml
spring:
  ai:
    model:
      embedding: oracle
    oracle:
      embedding:
        initialize-on-startup: true
        onnx-directory-alias: MODEL_DIR
        onnx-file: all_MiniLM_L12_v2.onnx
        onnx-model-name: ALL_MINILM_L12_V2
```

### 4) Initialize ONNX Model on Startup (Cloud URI)

```yaml
spring:
  ai:
    model:
      embedding: oracle
    oracle:
      embedding:
        initialize-on-startup: true
        onnx-model-name: ALL_MINILM_L12_V2_CLOUD
        onnx-credential: OCI_OSS_CRED
        onnx-uri: https://objectstorage.<region>.oraclecloud.com/n/<ns>/b/<bucket>/o/<model>.onnx
```

## Integration Tests In This Module

Main embedding container IT class:
- `src/test/java/org/springframework/ai/model/oracle/autoconfigure/OracleEmbeddingAutoConfigurationContainerIT.java`

Tests are enabled only when:
- `ORACLE_AUTOCONFIG_IT=true` (also accepts `1` or `yes`)

### ONNX Test File Requirement

Before running embedding container tests, you must add this ONNX file:
- `src/test/resources/models/all_MiniLM_L12_v2.onnx`

If you want to use a different ONNX model in tests, update:
- the test resource file under `src/test/resources/models/`
- ONNX constants in:
  - `OracleEmbeddingAutoConfigurationContainerIT`
  - `OracleEmbeddingAutoConfigurationObservationContainerIT`

Specifically keep these values aligned with your new model:
- `ONNX_RESOURCE`
- `ONNX_FILE`
- `ONNX_MODEL_NAME`

### Optional Cloud Test Variables

Used by cloud ONNX startup test:
- `ONNX_CLOUD_CREDENTIAL`
- `ONNX_CLOUD_URI`

Optional custom JDBC for cloud test path:
- `ORACLE_JDBC_URL`
- `ORACLE_USERNAME`
- `ORACLE_PASSWORD`

If custom JDBC is not provided, tests fall back to the local Testcontainers Oracle database.

### Run Tests

Run all tests for this module:

```bash
mvn -DORACLE_AUTOCONFIG_IT=true test
```

Run only embedding container integration test:

```bash
mvn -DORACLE_AUTOCONFIG_IT=true -Dtest=OracleEmbeddingAutoConfigurationContainerIT test
```

Run cloud ONNX startup scenario:

```bash
ONNX_CLOUD_CREDENTIAL=OCI_OSS_CRED \
ONNX_CLOUD_URI='https://objectstorage.<region>.oraclecloud.com/n/<ns>/b/<bucket>/o/<model>.onnx' \
mvn -DORACLE_AUTOCONFIG_IT=true -Dtest=OracleEmbeddingAutoConfigurationContainerIT test
```

## Troubleshooting

- `DBMS_VECTOR_CHAIN.UTL_TO_EMBEDDINGS is not available`:
  - verify Oracle version/features and privileges in the target DB.
- ONNX startup loading fails:
  - local mode: check directory alias, file placement, and DB privileges.
  - cloud mode: check credential name and object URI access.
- Startup fails with strategy conflict:
  - remove either local ONNX properties or cloud ONNX properties so only one strategy is configured.
