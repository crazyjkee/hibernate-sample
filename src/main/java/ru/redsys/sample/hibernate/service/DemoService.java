package ru.redsys.sample.hibernate.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.redsys.sample.hibernate.model.Comment;
import ru.redsys.sample.hibernate.model.Document;
import ru.redsys.sample.hibernate.repository.DocumentRepository;

@Service
public class DemoService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DocumentRepository repository;

    @Transactional
    public Document addDocument(String name, int commentsCount) {
        Document document = new Document(name);

        //создаем 2 комментария для документа
        document.setComments(createComments(document, commentsCount));

        entityManager.persist(document);

        entityManager.close();

        return document;
    }

    //создаем комментарии для документа
    private List<Comment> createComments(Document document, int commentsCount) {
        List<Comment> comments = new ArrayList();

        for (int commentNum = 0; commentNum < commentsCount; commentNum++) {
            comments.add(new Comment(String.format("Комментарий №%s к документу №%s",
                    commentNum, document.getId()),
                    document));
        }

        return comments;
    }

    @Transactional
    public void removeDocument(String name) {
        Document document = Optional.of(
                entityManager.createQuery("select d from Document d where d.name = :name", Document.class)
                        .setParameter("name", name)
                        .getSingleResult())
                .orElseThrow(() -> new RuntimeException("Документ не найден"));

        entityManager.remove(document);

        entityManager.close();
    }


    @Transactional
    public List<Comment> getAllDocumentsComments() {
        List<Comment> comments = new ArrayList();

        List<Document> documents = entityManager.createQuery("select d from Document d", Document.class)
                .getResultList();
        for (Document document : documents) {
            comments.addAll(document.getComments());
        }

        entityManager.close();

        return comments;
    }

    @Transactional
    public void updateDocumentName(Document document) {
        Document mergedDocument = document;

        //если объект document находится в состоянии detached - то запрашиваем его из БД
        if (!entityManager.contains(document)) {
            mergedDocument = entityManager.merge(document);
        }
        entityManager.persist(mergedDocument);

        entityManager.close();
    }

    public boolean existsDocumentByName(String name) {
        return repository.existsByName(name);
    }

    //чтение документов с пессимистческой блокировкой с таймаутом
    @Transactional
    public Document getDocumentById(int id, LockModeType lockModeType) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.lock.timeout", 1000L);

        Document document = entityManager.find(Document.class, id, lockModeType, properties);

        entityManager.close();
        return document;
    }

    @Transactional(readOnly = true)
    public Document getDocumentByEntityGraph(int documentId, String entityGraph) {
        EntityGraph graph = entityManager.getEntityGraph(entityGraph);

        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.fetchgraph", graph);

        Document document = entityManager.find(Document.class, documentId, properties);
        entityManager.close();

        return document;
    }
}
