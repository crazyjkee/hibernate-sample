import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.redsys.sample.hibernate.model.Document;
import ru.redsys.sample.hibernate.service.DemoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DemoService.class)
@EntityScan(basePackages = {"ru.redsys.sample.hibernate.model"})
@TestPropertySource("classpath:application.properties")
@SpringBootTest
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.redsys.sample.hibernate.repository")
public class DemoServiceTest {

    @Autowired
    private DemoService demoService;

    @Test
    public void addDocument() {
        Document document = demoService.addDocument("Тест создания документа. Документ №1", 2);

        Assert.assertNotEquals(0, document.getId());
    }

    @Test
    public void removeDocument() {
        String documentName = "Тест удаления документа. Документ №1";

        Document document = demoService.addDocument(documentName, 2);
        Assert.assertNotEquals(0, document.getId());

        demoService.removeDocument(documentName);

        Assert.assertEquals(false, demoService.existsDocumentByName(documentName));
    }

    //Одно из решений N + 1 problem. Извлечение комментариев документа при итерации каждые 3 шага
    @Test
    public void batchingChildren() {
        for (int numDocument = 1; numDocument <= 10; numDocument++) {
            demoService.addDocument("Тест решения N + 1. Документ №" + numDocument, 2);
        }

        Assert.assertEquals(20, demoService.getAllDocumentsComments().size());
    }

    @Test(expected = OptimisticLockException.class)
    public void versioning() {
        Document document = demoService.addDocument("Тест версионности. Документ №1", 0);

        demoService.updateDocumentName(document);

        //ловим ошибку версионности. В документе версия другая чем в БД
        document.setName("Документ изменился второй раз");
        demoService.updateDocumentName(document);
    }

    @Test
    public void pessimisticLock(){
        //создание нового документа
        Document document = demoService.addDocument("Тест пессимистической блокировки. Документ №1", 0);

        //блокируем на чтенеие
        demoService.getDocumentById(document.getId(), LockModeType.PESSIMISTIC_READ);
        //блокируем на запись
        demoService.getDocumentById(document.getId(), LockModeType.PESSIMISTIC_WRITE);
    }


    @Test
    public void entityGraph(){
        Document document = demoService.addDocument("Тест графов. Документ №1", 2);

        demoService.getDocumentByEntityGraph(document.getId(),"document-comments-entity-graph");

    }


}

