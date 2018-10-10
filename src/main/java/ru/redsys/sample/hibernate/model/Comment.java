package ru.redsys.sample.hibernate.model;

import java.io.Serializable;
import javax.persistence.*;


@Entity
@Table
public class Comment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String content;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;


    public Comment() {
    }

    public Comment(String content, Document document) {
        this.content = content;
        this.document = document;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
