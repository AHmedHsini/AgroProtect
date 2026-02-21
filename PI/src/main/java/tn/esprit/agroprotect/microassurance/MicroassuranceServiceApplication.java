package tn.esprit.agroprotect.microassurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MicroassuranceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroassuranceServiceApplication.class, args);
    }

}