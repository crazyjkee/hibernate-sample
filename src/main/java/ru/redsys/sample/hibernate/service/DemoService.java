package ru.redsys.sample.hibernate.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redsys.sample.hibernate.model.Comment;
import ru.redsys.sample.hibernate.model.Document;
import ru.redsys.sample.hibernate.repository.DocumentRepository;

@Service
public class DemoService {

    private static Logger LOG = LoggerFactory.getLogger(DemoService.class);
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DocumentRepository repository;


    @Autowired
    private Demo2Service demo2Service;

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

        List<Document> documents =
                entityManager.createQuery("select d from Document d", Document.class).getResultList();

        LOG.warn("Всего найдено {} документов", documents.size());
        for (Document document : documents) {
            LOG.warn("Получаем комментарии документа №{}", document.getId());
            comments.addAll(document.getComments());
        }

        entityManager.close();

        return comments;
    }

    @Transactional
    public void updateDocumentName(Document document) {
        //тут поймаем optimistic lock exception если в БД версия документа изменилась
        Document mergedDocument = entityManager.merge(document);

        entityManager.persist(mergedDocument);

        entityManager.close();
    }

    public boolean existsDocumentByName(String name) {
        return repository.existsByName(name);
    }

    //чтение документов с пессимистческой блокировкой с таймаутом
    @Transactional(timeout = 5) //время работы транзакции не более 5 секунд
    public Document getDocumentById(int id, LockModeType lockModeType, int sleepInSeconds) throws Exception {
        Map<String, Object> properties = new HashMap<>();

        //если значение 0 - то пытаемся получить блокировку сразу(если не получится, будет ошибка)
        //Для Oracle можно задавать значение > 0, которое укажет время ожидание получения блокировки
        properties.put("javax.persistence.lock.timeout", 0);

        LOG.warn("Получаем документ с номером {}", id);
        Document document = entityManager.find(Document.class,
                id,
                lockModeType,
                properties);
        LOG.warn("Получили документ с номером {}", document.getId());

        document.setName("Получили документ с режимом спячки в " + sleepInSeconds + " секунд");

        entityManager.persist(document);

        if (sleepInSeconds > 0) {
            LOG.warn("Спим {} секунд", sleepInSeconds);
            Thread.sleep(sleepInSeconds * 1000);
        }

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

    @Transactional
    public void addComment(int documentId, String commentContent) {
        //получаем proxy объект
        Document document = entityManager.getReference(Document.class, documentId);

        LOG.warn("Получаем название документа");

        //запрос в бд будет сейчас, если использовать метод getReference и получать значения полей через прокси
        //если использовать метод find, то запрос в бд пойдет сразу
        document.getName();

        LOG.warn("Получаем все комментарии");

        //Получаем комментарии из бд
        document.getComments().get(0).getContent();

        LOG.warn("Добавляем новый комментарий");

        entityManager.persist(new Comment(commentContent, document));
    }

    @Transactional
    public void cache1Level(int documentId) {
        Document document1 = entityManager.find(Document.class, documentId);

        LOG.warn("Название документа первой версии - {}", document1.getName());

        document1.setName("Кеш документа версия 2");

        entityManager.persist(document1);//не вызовется


        Document document2 = entityManager.find(Document.class, documentId);

        LOG.warn("Название документа второй версии - {}", document2.getName());
        document2.setName("Кеш документа версия 3");

        entityManager.persist(document2); // не сработает


        Document document3 = entityManager.find(Document.class, documentId);

        LOG.warn("Название документа третьей версии - {}", document3.getName());

        entityManager.persist(document3); // сработает!!!

        //берем из другого сервиса документ по его номеру
        Document documentFromAnotherService = demo2Service.getDocumentById(documentId);

        LOG.warn("Кеш работает: " +
                (document1 == document2
                        && document2 == document3
                        && document3 == documentFromAnotherService));
    }
}
