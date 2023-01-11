package be.jyl.entities;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
public class Categories {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_category", nullable = false)
    private int idCategory;
    @Basic
    @Column(name = "categoryName", nullable = false, length = 100)
    private String categoryName;
    @OneToMany(mappedBy = "categoriesByIdCategory")
    private Collection<Articles> articlesByIdCategory;

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categories that = (Categories) o;
        return idCategory == that.idCategory && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategory, categoryName);
    }

    public Collection<Articles> getArticlesByIdCategory() {
        return articlesByIdCategory;
    }

    public void setArticlesByIdCategory(Collection<Articles> articlesByIdCategory) {
        this.articlesByIdCategory = articlesByIdCategory;
    }
}