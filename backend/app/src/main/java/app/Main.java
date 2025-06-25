package app;

import app.clientes.EntidadCliente;
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

        EntidadCliente cliente = new EntidadCliente(contexto);
        cliente.run();
    }
}