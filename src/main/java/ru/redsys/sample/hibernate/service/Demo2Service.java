package ru.redsys.sample.hibernate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redsys.sample.hibernate.model.Document;

import javax.persistence.EntityManager;

@Service
public class Demo2Service {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Document getDocumentById(int documentId) {
        return entityManager.find(Document.class, documentId);
    }
}
