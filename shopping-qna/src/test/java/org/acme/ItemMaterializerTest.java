package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.importer.ItemMaterializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

@QuarkusTest
public class ItemMaterializerTest {

    @Inject
    ItemMaterializer itemMaterializer;

    @Test
    public void shouldMaterializeItem() throws IOException {
        Item i = new Item("a", "b", "c", "d", List.of("e"), "f");
        itemMaterializer.saveItem(i);
    }

}
