import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ru.redsys.sample.hibernate.service.DemoService;

/**
 * Created by Artem Pasyugin on 10.10.2018.
 */
@RunWith(SpringRunner.class)
@EnableJpaRepositories(basePackages = "{ru.redsys.sample.hibernate", entityManagerFactoryRef = "emf")
@EntityScan(basePackages = {"ru.redsys.sample.hibernate.model"})
@TestPropertySource("classpath:application.properties")
@SpringBootTest(classes = DemoService.class)
public class DemoServiceTest {

    @Autowired
    private DemoService demoService;


    @Test
    public void addArticle() {
//        demoService.getArticles();
    }

    @Test
    public void getArticles() {

    }
}

