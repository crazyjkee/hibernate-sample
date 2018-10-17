import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.redsys.sample.hibernate.service.Demo2Service;
import ru.redsys.sample.hibernate.service.DemoService;

import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DemoService.class, Demo2Service.class})
@EntityScan(basePackages = {"ru.redsys.sample.hibernate.model"})
@TestPropertySource("classpath:application.properties")
@SpringBootTest
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.redsys.sample.hibernate.repository")
public class DemoServiceTest {

    private static Logger LOG = LoggerFactory.getLogger(DemoServiceTest.class);
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
        Document document = demoService.addDocument("Тест версионности. Документ №1", 2);

        LOG.warn("Версия документа {}", document.getVersion());
        document.setName("Документ измениллся первый раз");
        demoService.updateDocumentName(document);


        LOG.warn("Версия документа {} не изменилась", document.getVersion());
        //ловим ошибку версионности. В документе версия другая чем в БД
        document.setName("Документ изменился второй раз");
        demoService.updateDocumentName(document);
    }


    //пессимистическая блокировка чтение и изменения конкретного документа
    @Test
    public void pessimisticLock() throws Exception {
        //создание нового документа
        Document document = demoService.addDocument("Тест пессимистической блокировки. Документ №1", 2);

        Thread firstThread = new Thread(() -> {
            try {
                int sleepInSeconds = 10;
                LOG.warn("Транзакция номер 1. После получения документа - спим {} секунд", sleepInSeconds);
                demoService.getDocumentById(document.getId(), LockModeType.PESSIMISTIC_WRITE, sleepInSeconds);
                LOG.warn("Транзакция номер 1. Завершена");
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }

        });
        firstThread.start();

        Thread secondThread = new Thread(() -> {
            try {
                //запускаем через секунду
                Thread.sleep(1000);

                LOG.warn("Транзакция номер 2");
                demoService.getDocumentById(document.getId(), LockModeType.PESSIMISTIC_WRITE, 0);
                LOG.warn("Транзакция номер 2. Завершена");
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });
        secondThread.start();

        firstThread.join();
    }


    @Test
    public void entityGraph() {
        Document document = demoService.addDocument("Тест графов. Документ №1", 2);

        demoService.getDocumentByEntityGraph(document.getId(), "document-comments-entity-graph");

    }

    //пример прокси доменной модели
    @Test
    public void testProxyDocument() {
        Document document = demoService.addDocument("Тест прокси документа", 0);

        demoService.addComment(document.getId(), "Добавление комментария в тестовый документ");
    }

    //пример кеша 1 уровня
    @Test
    public void testFirstLevelCache() {
        Document document = demoService.addDocument("Тест кеша документа", 1);

        //демонстрация persistence cache (read repeatable isolation)
        demoService.cache1Level(document.getId());
    }


}

