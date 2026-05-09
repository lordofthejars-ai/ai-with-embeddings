package org.acme;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusClient;
import io.milvus.param.dml.InsertParam;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jboss.logging.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class EmailDatasetParser {

    public static List<Document> load(String csv) throws IOException {
        try (Reader reader = new FileReader(csv);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                 .withFirstRecordAsHeader())) {

            return csvParser
                .stream()
                .map(r ->
                    Document.from(
                        r.get("text"),
                        Metadata.from("label", r.get("label"))
                    )
                )
                .limit(500)
                .toList();
        }
    }
}
