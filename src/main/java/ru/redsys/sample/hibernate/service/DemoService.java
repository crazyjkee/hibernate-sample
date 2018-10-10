package ru.redsys.sample.hibernate.service;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redsys.sample.hibernate.model.Comment;
import ru.redsys.sample.hibernate.model.Document;

@Service
public class DemoService {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Document addDocument(){
        Document document = new Document("Document №1");

        List<Comment> comments = new ArrayList();

        comments.add(new Comment("Комментарий №1", document));
        comments.add(new Comment("Комментарий №2",document));
        document.setComments(comments);

        entityManager.persist(document);

        return document;
    }

    @Transactional
    public void removeDocument(Document document){
        //Объект документа взят из другой транзакции. Считается, что у него состояние detached
        Document mergedDocument = entityManager.merge(document);
        entityManager.remove(mergedDocument);
    }

}
