package org.acme.persistence;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;

import java.util.List;
import java.util.Optional;

@Entity
@NamedQuery(name = "Product.findAllGroupByType", query = "FROM Product p ORDER BY p.type")
public class Product extends PanacheEntity {

    public String type;

    @Column(unique = true)
    public String name;

    @Lob
    public String summary;

    @Lob
    public String imageSummary;

    @ElementCollection
    public List<String> tags;
    @Lob
    public String image;

    public static Product findProductByName(String name) {
        return find("name", name).firstResult();
    }

    public static List<Product> findAllProductsGroupedByType() {
        return find("#Product.findAllGroupByType").list();
    }

}
