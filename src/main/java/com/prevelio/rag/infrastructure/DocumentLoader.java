package com.prevelio.rag.infrastructure;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import io.quarkus.runtime.Startup;

import dev.langchain4j.data.document.DocumentSplitter;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@ApplicationScoped
@Slf4j
public class DocumentLoader {

    private EmbeddingStore<TextSegment> embeddingStore;

    @Startup
    void onStart() {
        this.embeddingStore = loadDocuments("documents/terms-of-use.txt");
    }

    @Produces
    public EmbeddingStore<TextSegment> embeddingStore() {
        return embeddingStore;
    }

    public EmbeddingStore<TextSegment> loadDocuments(String documentPath) {
        Document document = loadDocument(toPath(documentPath), new TextDocumentParser());

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);
        List<TextSegment> segments = splitter.split(document);
        log.info("Number of document splits: {}", segments.size());
        segments.forEach(segment -> log.info("Segment: {}", segment.text()));

        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
        store.addAll(response.content(), segments);

        log.info("Ingestion result: {}", response.tokenUsage());

        return store;
    }

    private Path toPath(String fileName) {
        URL resource = DocumentLoader.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + fileName);
        }
        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to convert resource URL to URI: " + fileName, e);
        }
    }

}
