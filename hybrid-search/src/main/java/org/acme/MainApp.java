package org.acme;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;

public class MainApp {

    public static final String CONTENT = "content";
    static Directory directory;
    static EmbeddingModel embeddingModel;
    static InMemoryEmbeddingStore<TextSegment> store;
    static Analyzer analyzer;

    public static void main(String[] args) throws Exception {

        store = new InMemoryEmbeddingStore<>();
        embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        PathMatcher txtFiles = FileSystems.getDefault().getPathMatcher("glob:*.txt");
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(Paths.get("src/main/resources"),
                txtFiles, new TextDocumentParser());

        ingest(documents);

        List<Content> contents = hybridRetriever()
                .retrieve(dev.langchain4j.rag.query.Query.from("What does error PAY-517 mean?"));

        System.out.println("----- Final Results Ordered -----");
        contents.forEach(c -> System.out.println(c.textSegment().text()));

    }

    private static void ingest(List<Document> documents) throws IOException {
        EmbeddingStoreIngestor embeddingIngestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build();

        embeddingIngestor.ingest(documents);

        directory = new ByteBuffersDirectory();
        analyzer = new StandardAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        for (Document doc : documents) {
            org.apache.lucene.document.Document d = new org.apache.lucene.document.Document();
            d.add(new TextField(CONTENT, doc.text(), Field.Store.YES));
            writer.addDocument(d);
        }

        writer.commit();
    }

    private static ContentRetriever semanticRetriever() {
        return
                EmbeddingStoreContentRetriever.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(store)
                        .minScore(0.75)
                        .maxResults(1)
                        .build();
    }

    private static ContentRetriever bm25Retriever() throws IOException {
        return new Bm25ContentRetriever(DirectoryReader.open(directory), analyzer, 1);
    }

    private static ContentRetriever hybridRetriever() throws IOException {
        return new HybridRrfRetriever(semanticRetriever(), bm25Retriever(), 60, 2);
    }

    static class Bm25ContentRetriever implements ContentRetriever {

        private IndexSearcher searcher;
        private IndexReader reader;
        private Analyzer analyzer;
        private int topK;

        public Bm25ContentRetriever(IndexReader indexReader, Analyzer analyzer, int topK) throws IOException {
            // ;
            this.reader = indexReader;
            this.searcher = new IndexSearcher(this.reader);
            this.searcher.setSimilarity(new BM25Similarity());
            this.topK = topK;
            this.analyzer = analyzer;
        }

        @Override
        public List<Content> retrieve(dev.langchain4j.rag.query.Query query) {
            QueryParser parser = new QueryParser(CONTENT, analyzer);
            try {
                Query q = parser.parse(query.text());
                TopDocs topDocs = searcher.search(q, topK);
                StoredFields storedFields = reader.storedFields();

                List<Content> results = new ArrayList<>();
                for (ScoreDoc hit : topDocs.scoreDocs) {
                    org.apache.lucene.document.Document d = storedFields.document(hit.doc);
                    String content = d.get(CONTENT);
                    results.add(Content.from(content));
                }
                return results;
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    static class HybridRrfRetriever implements ContentRetriever {

        private final ContentRetriever semanticRetriever;
        private final ContentRetriever bm25Retriever;
        private final int k;
        private final int topK;

        public HybridRrfRetriever(
                ContentRetriever semanticRetriever,
                ContentRetriever bm25Retriever,
                int k,
                int topK
        ) {
            this.semanticRetriever = semanticRetriever;
            this.bm25Retriever = bm25Retriever;
            this.k = k;
            this.topK = topK;
        }

        @Override
        public List<Content> retrieve(dev.langchain4j.rag.query.Query query) {
            List<Content> semanticResults = semanticRetriever.retrieve(query);

            System.out.println("----- Semantic Results -----");
            semanticResults.forEach(c -> System.out.println(c.textSegment().text()));
            System.out.println("-".repeat(80));

            List<Content> bm25Results = bm25Retriever.retrieve(query);

            System.out.println("----- BM25 Results -----");
            bm25Results.forEach(c -> System.out.println(c.textSegment().text()));
            System.out.println("-".repeat(80));

            Map<String, ScoredContent> fused = new HashMap<>();

            applyRrf(fused, semanticResults);
            applyRrf(fused, bm25Results);

            return fused.values().stream()
                    .sorted(Comparator.comparingDouble(ScoredContent::score).reversed())
                    .limit(topK)
                    .map(ScoredContent::content)
                    .toList();
        }

        private void applyRrf(
                Map<String, ScoredContent> fused,
                List<Content> results
        ) {
            for (int i = 0; i < results.size(); i++) {
                Content content = results.get(i);
                double rrfScore = 1.0 / (k + i + 1);

                fused.compute(content.textSegment().text(), (key, existing) -> {
                    if (existing == null) {
                        return new ScoredContent(content, rrfScore);
                    }
                    existing.add(rrfScore);
                    return existing;
                });
            }
        }
    }

    private static class ScoredContent {
        private final Content content;
        private double score;

        ScoredContent(Content content, double score) {
            this.content = content;
            this.score = score;
        }

        void add(double value) {
            this.score += value;
        }

        double score() {
            return score;
        }

        Content content() {
            return content;
        }
    }
}
