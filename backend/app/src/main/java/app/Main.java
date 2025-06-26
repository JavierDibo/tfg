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

        ClienteEntidad cliente = contexto.getBean(ClienteEntidad.class);
        
        // Ejemplo 1: Esto debería fallar por ID inválido
        try {
            System.out.println("=== Probando con ID inválido ===");
            cliente.ejecutarOperacion(-1, "Hola mundo");
        } catch (Exception e) {
            System.out.println("Error capturado: " + e.getMessage());
        }
        
        // Ejemplo 2: Esto debería fallar por texto vacío
        try {
            System.out.println("\n=== Probando con texto vacío ===");
            cliente.ejecutarOperacion(1, "");
        } catch (Exception e) {
            System.out.println("Error capturado: " + e.getMessage());
        }
        
        // Ejemplo 3: Esto debería funcionar correctamente
        try {
            System.out.println("\n=== Probando con parámetros válidos ===");
            cliente.ejecutarOperacion(1, "Hola mundo");
            System.out.println("Operación ejecutada exitosamente");
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
        
        // Ejemplo 4: Probando configuración personalizada
        try {
            System.out.println("\n=== Probando operación sin validar ID ===");
            cliente.operacionSinValidarId(-5, "Texto válido");
            System.out.println("Operación sin validar ID ejecutada exitosamente");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}