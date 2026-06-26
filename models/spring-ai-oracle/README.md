# Spring AI Oracle: Loader + Splitter + Embedding

This module provides three building blocks for Oracle-based RAG pipelines:

1. `OracleDocumentReader` to load documents from files/directories or Oracle tables
2. `OracleDocumentSplitter` to chunk text with `DBMS_VECTOR_CHAIN.UTL_TO_CHUNKS`
3. `OracleEmbeddingModel` to generate vectors with `DBMS_VECTOR_CHAIN.UTL_TO_EMBEDDINGS`

## 1) Prerequisites

1. Oracle Database 23ai (or compatible setup) with vector features enabled.
2. A JDBC user that can run:
   - `DBMS_VECTOR_CHAIN.UTL_TO_TEXT`
   - `DBMS_VECTOR_CHAIN.UTL_TO_CHUNKS`
   - `DBMS_VECTOR_CHAIN.UTL_TO_EMBEDDINGS`
3. Java 17+ and Maven.
4. Oracle JDBC driver available via Maven (already declared in this module `pom.xml`).

## 2) Add Dependency

If you are consuming this module from another project, include:

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-oracle</artifactId>
  <version>${spring-ai.version}</version>
</dependency>
```

## 3) Create a DataSource

```java
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

DataSource oracleDataSource(String jdbcUrl, String username, String password) {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setUrl(jdbcUrl);
    ds.setUsername(username);
    ds.setPassword(password);
    return ds;
}
```

## 4) Load Documents With `OracleDocumentReader`

### 4.1 Load from a local file

```java
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.oracle.loader.OracleDocumentReader;

DataSource dataSource = oracleDataSource(jdbcUrl, username, password);
Path file = Path.of("/path/to/docs/story.md");

OracleDocumentReader reader = new OracleDocumentReader(dataSource, file);
List<Document> documents = reader.get();
```

### 4.2 Load from a directory recursively

```java
Path rootDir = Path.of("/path/to/docs");
OracleDocumentReader reader = new OracleDocumentReader(dataSource, rootDir);
List<Document> documents = reader.get();
```

### 4.3 Load from a database table

```java
OracleDocumentReader reader = OracleDocumentReader.builder(dataSource, "APP", "DOCS", "TEXT")
        .preferences(org.springframework.ai.oracle.loader.OracleDocumentPreferences.builder()
                .plaintext(true)
                .format("TEXT")
                .build())
        .build();

List<Document> documents = reader.get();
```

### 4.4 What to prepare for table-based loading

Before using `OracleDocumentReader.builder(dataSource, owner, tableName, columnName)`, make sure:

1. The table exists in the `owner` schema.
2. The text column exists and contains document content (`CLOB`/text payload).
3. The JDBC user can read the table.
4. The JDBC user can execute `DBMS_VECTOR_CHAIN.UTL_TO_TEXT`.
5. Use valid Oracle identifiers for `owner`, `tableName`, and `columnName` (letters/numbers/`_`/`$`/`#`, starting with a letter).

Example setup:

```sql
-- Run as table owner
create table DOCS (
  ID number primary key,
  TEXT clob
);

insert into DOCS (ID, TEXT) values (1, 'first document');
insert into DOCS (ID, TEXT) values (2, 'second document');
commit;

-- If another user reads this table, grant access
grant select on DOCS to APP_USER;

-- Grant package execute when needed (run with appropriate privileges)
grant execute on DBMS_VECTOR_CHAIN to APP_USER;
```

Then in Java:

```java
String owner = "APP"; // schema name in uppercase
OracleDocumentReader reader = OracleDocumentReader.builder(dataSource, owner, "DOCS", "TEXT").build();
List<Document> documents = reader.get();
```

### 4.5 Optional conversion preferences

The same `OracleDocumentPreferences` options work for both source types:
- Resource/file loading
- Table loading

Resource/file example:

```java
import org.springframework.ai.oracle.loader.OracleDocumentPreferences;

OracleDocumentReader reader = OracleDocumentReader.builder(dataSource, new org.springframework.core.io.FileSystemResource(file))
        .preferences(OracleDocumentPreferences.builder()
                .plaintext(true)
                .charset("UTF8")
                .format("TEXT")
                .build())
        .build();
```

Table example:

```java
OracleDocumentReader reader = OracleDocumentReader.builder(dataSource, "APP", "DOCS", "TEXT")
        .preferences(OracleDocumentPreferences.builder()
                .plaintext(true)
                .charset("UTF8")
                .format("TEXT")
                .build())
        .build();
```

Notes:
- `format` supports: `BINARY`, `TEXT`, `IGNORE`.
- Loaded docs include metadata like `file_name`, `absolute_directory_path`, and `source` for resource-based loading.
- Oracle reference for `UTL_TO_TEXT`:
  https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/utl_to_text.html

## 5) Split Documents With `OracleDocumentSplitter`

### 5.1 Default splitting

```java
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.oracle.chunking.OracleDocumentSplitter;

OracleDocumentSplitter splitter = new OracleDocumentSplitter(dataSource);
List<Document> chunks = splitter.split(documents);
```

### 5.2 Split with explicit chunking preferences

```java
import org.springframework.ai.oracle.chunking.OracleChunkingPreferences;

OracleDocumentSplitter splitter = OracleDocumentSplitter.builder(dataSource)
        .preferences(OracleChunkingPreferences.builder()
                .by("words")
                .max(200)
                .overlap(20)
                .split("sentence")
                .build())
        .build();

List<Document> chunks = splitter.split(documents);
```

### 5.3 Split using Oracle vocabulary

Use this mode when you already created an Oracle vocabulary and you want chunking boundaries to follow that vocabulary.

```java
import org.springframework.ai.oracle.chunking.OracleDocumentSplitter;
import org.springframework.ai.oracle.chunking.OracleChunkingPreferences;

OracleDocumentSplitter splitter = OracleDocumentSplitter.builder(dataSource)
        .preferences(OracleChunkingPreferences.builder()
                .by("vocabulary")
                .vocabulary("MY_VOCABULARY")
                .max(200)
                .overlap(20)
                .build())
        .build();

List<Document> vocabChunks = splitter.split(documents);
```

Important:
- `vocabulary(...)` requires `by("vocabulary")`.
- You must create and populate the Oracle vocabulary table/object first (for example `MY_VOCABULARY`) before running the splitter.
- `MY_VOCABULARY` must already exist in Oracle and be visible to your JDBC user.
- You can combine `vocabulary` mode with other options such as `max`, `overlap`, and `language`.

Example setup (run in Oracle before Java split calls):

```sql
-- Create and populate your vocabulary object/table in Oracle first.
-- Use your Oracle vocabulary DDL/process, then grant read access to the app user.
-- Example name used in Java: MY_VOCABULARY
```

End-to-end example:

```java
OracleDocumentReader reader = new OracleDocumentReader(dataSource, Path.of("/path/to/docs"));
List<Document> docs = reader.get();

OracleDocumentSplitter vocabSplitter = OracleDocumentSplitter.builder(dataSource)
        .preferences(OracleChunkingPreferences.builder()
                .by("vocabulary")
                .vocabulary("MY_VOCABULARY")
                .build())
        .build();

List<Document> vocabChunks = vocabSplitter.split(docs);
```

Notes:
- Oracle reference for `UTL_TO_CHUNKS`:
  https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/utl_to_chunks-dbms_vector_chain.html

## 6) Generate Embeddings With `OracleEmbeddingModel`

### 6.1 Create model options

```java
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.oracle.embedding.OracleEmbeddingOptions;
import org.springframework.ai.oracle.embedding.OracleEmbeddingPreferences;

OracleEmbeddingOptions options = OracleEmbeddingOptions.builder()
        .model("database")
        .preferences(OracleEmbeddingPreferences.builder()
                .provider("database")
                .model("database")
                .build())
        .batching(true)
        .metadataMode(MetadataMode.EMBED)
        .build();
```

Why `model` appears twice:
- `OracleEmbeddingOptions.model(...)` is used by Spring AI for request validation, response metadata (`response.getMetadata().getModel()`), and some initialization fallbacks.
- `OracleEmbeddingPreferences.model(...)` is sent to Oracle inside the JSON preferences payload and controls database embedding behavior.
- For custom models, keep both values aligned.
- For default database behavior, you can usually rely on defaults.

### 6.2 Create embedding model

```java
import org.springframework.ai.oracle.embedding.OracleEmbeddingModel;

OracleEmbeddingModel embeddingModel = new OracleEmbeddingModel(dataSource, options);
```

### 6.2.1 Optional ONNX startup loading

When `initializeOnStartup` is enabled, cloud loading requires an explicit `onnxUri`.

- Cloud behavior: set `onnxUri` (and optional `onnxCredential`).
- User local file behavior: set both `onnxDirectoryAlias` and `onnxFile` to load from Oracle directory file storage.

For cloud ONNX integration tests, set:

- `ONNX_CLOUD_URI` (required): full Object Storage URI to the `.onnx` file.
- `ONNX_CLOUD_CREDENTIAL` (optional): Oracle credential name when required by the URI.

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
- `ONNX_MODEL_NAME` in readme

### 6.3 Embed a string

```java
float[] vector = embeddingModel.embed("Hello from Oracle");
```

### 6.4 Embed chunked documents

```java
for (Document chunk : chunks) {
    float[] vector = embeddingModel.embed(chunk);
    // persist vector + chunk text + metadata in your vector store/table
}
```

### 6.5 Batch embedding request

```java
import java.util.List;

import org.springframework.ai.embedding.EmbeddingRequest;

var response = embeddingModel.call(new EmbeddingRequest(
        List.of("chunk one", "chunk two"),
        options
));

int count = response.getResults().size();
```

Oracle reference for `UTL_TO_EMBEDDING` and `UTL_TO_EMBEDDINGS`:
https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/utl_to_embedding-and-utl_to_embeddings-dbms_vector_chain.html

## 7) End-to-End Example (Loader -> Splitter -> Embedding)

```java
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.oracle.chunking.OracleDocumentSplitter;
import org.springframework.ai.oracle.chunking.OracleChunkingPreferences;
import org.springframework.ai.oracle.embedding.OracleEmbeddingModel;
import org.springframework.ai.oracle.embedding.OracleEmbeddingOptions;
import org.springframework.ai.oracle.embedding.OracleEmbeddingPreferences;
import org.springframework.ai.oracle.loader.OracleDocumentReader;

DataSource dataSource = oracleDataSource(jdbcUrl, username, password);

// 1) Load
OracleDocumentReader reader = new OracleDocumentReader(dataSource, Path.of("/path/to/docs"));
List<Document> docs = reader.get();

// 2) Split
OracleDocumentSplitter splitter = OracleDocumentSplitter.builder(dataSource)
        .preferences(OracleChunkingPreferences.builder().by("words").max(200).overlap(20).build())
        .build();
List<Document> chunks = splitter.split(docs);

// 3) Embed
OracleEmbeddingOptions embeddingOptions = OracleEmbeddingOptions.builder()
        .model("database")
        .preferences(OracleEmbeddingPreferences.builder().provider("database").model("database").build())
        .batching(true)
        .build();

OracleEmbeddingModel embeddingModel = new OracleEmbeddingModel(dataSource, embeddingOptions);

for (Document chunk : chunks) {
    float[] vector = embeddingModel.embed(chunk);
    // Save vector + chunk metadata
}
```
## 8) Oracle Documentation Links

- UTL_TO_TEXT:
  https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/utl_to_text.html
- UTL_TO_EMBEDDING and UTL_TO_EMBEDDINGS:
  https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/utl_to_embedding-and-utl_to_embeddings-dbms_vector_chain.html
- UTL_TO_CHUNKS:
  https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/utl_to_chunks-dbms_vector_chain.html
