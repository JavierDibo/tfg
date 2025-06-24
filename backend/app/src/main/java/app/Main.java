package app;

import jakarta.persistence.Entity;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages =  {
        "app.dtos",
        "app.repositorios",
        "app"
})
@EntityScan(basePackages = "app.entidades")
@EnableJpaRepositories(basePackages = "app.repositorios")
public class Main {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.setBannerMode(Banner.Mode.OFF);
        ApplicationContext contexto = app.run(args);

        EntidadCliente cliente = new EntidadCliente(contexto);
        cliente.run();
    }
}