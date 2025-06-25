package app;

import app.clientes.ClienteEntidad;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication()
public class Main {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.setBannerMode(Banner.Mode.OFF);
        ApplicationContext contexto = app.run(args);

        ClienteEntidad cliente = new ClienteEntidad(contexto);
        cliente.run();
    }
}