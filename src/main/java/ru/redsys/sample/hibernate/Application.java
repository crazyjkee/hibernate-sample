package ru.redsys.sample.hibernate;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;
import ru.redsys.sample.hibernate.model.Comment;
import ru.redsys.sample.hibernate.model.Document;
import ru.redsys.sample.hibernate.service.DemoService;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private DemoService demoService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Document document = demoService.addDocument();
        demoService.removeDocument(document);
    }

/*    @Transactional
    public Document addDocument(){
        Document document = new Document("Document №1");

        List<Comment> comments = new ArrayList();

        comments.add(new Comment("Комментарий №1", document));
        comments.add(new Comment("Комментарий №2", document));

        document.setComments(comments);

        entityManager.persist(document);

        return document;
    }

    @Transactional
    public void removeDocument(){
        entityManager.remove(addDocument());
    }*/

}
