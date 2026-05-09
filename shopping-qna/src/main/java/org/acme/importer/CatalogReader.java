package org.acme.importer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;

import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Singleton
public class CatalogReader {

    public record Product(
                          String articleType, String productName, byte[] image){}

    public List<Product> readRandomProducts(String parquetFile) {

        final Path path = new Path(parquetFile);
        final Configuration conf = new Configuration();

        List<Product> products = new ArrayList<>();

        try (ParquetFileReader reader = ParquetFileReader.open(conf, path)) {
            // Get schema and metadata
            MessageType schema = reader.getFooter().getFileMetaData().getSchema();
            int totalRowGroups = reader.getRowGroups().size();

            String articleTypeColumnName = schema.getFields().get(2).getName();
            String productDisplayNameColumnName = schema.getFields().get(3).getName();
            String imageColumnName = schema.getFields().get(0).getName();

            for (int i = 0; i < totalRowGroups; i++) {

                // Move to that group
                PageReadStore pages = reader.readNextRowGroup();
                if (pages == null) continue;

                MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
                RecordReader<Group> recordReader = columnIO.getRecordReader(
                        pages, new GroupRecordConverter(schema));

                int rowCount = (int) pages.getRowCount();
                for (int j = 0; j < rowCount; j++) {
                    Group group = recordReader.read();
                    if (group != null) {

                        String articleType = group.getString(articleTypeColumnName, 0);
                        String productDisplayName = group.getString(productDisplayNameColumnName, 0);

                        byte[] pic = group.getGroup(imageColumnName, 0)
                                            .getBinary(0, 0).getBytes();

                        products.add(
                                new Product(articleType,
                                        productDisplayName, pic));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Random random = new Random();
        List<Product> randomProducts = new ArrayList<>();

        for (int i=0; i<20; i++) {
            randomProducts.add(products.get(random.nextInt(products.size())));
        }

        return randomProducts;
    }


}
