package org.acme;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.persistence.Product;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Path("/api/products")
public class ProductsResource {

    public record ProductResponse(String name, String description, List<String> tags, String image){}

    @Inject
    Ingestor ingestor;

    @Inject
    Retriever retriever;

    @Startup
    public void populate() throws IOException {
        ingestor.populate();
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductResponse> search(Map<String, String> query) {
        if (query.isEmpty()) {
            return Product
                    .findAllProductsGroupedByType()
                    .stream()
                    .map(p ->
                            new ProductResponse(p.name, p.summary,
                                    p.tags, "data:image/png;base64, " + p.image)
                    )
                    .toList();

        } else {
            String searchContent = query.get("search");

            if (searchContent.startsWith("data:image/jpeg;base64,")) {
                searchContent = searchContent.substring(23);
            }

            return retriever.retrieve(searchContent)
                    .stream()
                    .map(Product::findProductByName)
                    .filter(Objects::nonNull)
                    .map(p ->
                            new ProductResponse(p.name, p.summary,
                                    p.tags, "data:image/png;base64, " + p.image)
                    )
                    .toList();
        }
    }
}
