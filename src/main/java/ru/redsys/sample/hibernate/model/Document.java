package ru.redsys.sample.hibernate.model;


import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OptimisticLock;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NamedEntityGraph(name = "document-comments-entity-graph",
        attributeNodes = @NamedAttributeNode("comments"))
public class Document implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "document")
    @BatchSize(size = 3)
    private List<Comment> comments;

    @Version
    private long version;


    public Document() {
    }

    public Document(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public long getVersion() {
        return version;
    }

}
