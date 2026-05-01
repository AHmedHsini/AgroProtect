package  tn.esprit.agroprotect.Marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroprotectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroprotectApplication.class, args);
    }

}
