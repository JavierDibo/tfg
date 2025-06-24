package app;

import app.dtos.EntidadDTO;
import app.entidades.Entidad;
import app.servicios.EntidadServicio;
import jakarta.activation.DataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class EntidadCliente {

    @Bean
    SingleConnectionDataSource dataSource() {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUsername("admin");
        dataSource.setPassword("admin-jfdg-admin");

        return dataSource;
    }

    ApplicationContext appContext;
    EntidadServicio entidadServicio;

    public EntidadCliente(ApplicationContext appContext) {
        this.appContext = appContext;
        entidadServicio = appContext.getBean(EntidadServicio.class);
    }

    public void run() {

        System.out.println("=== TESTING ENTIDAD OPERATIONS ===");

        // 1. Create an entity and convert to DTO
        System.out.println("\n1. Creating new EntidadDTO...");
        EntidadDTO nuevaEntidadDTO = new EntidadDTO("Test Info - Created at startup - " + System.currentTimeMillis());
        System.out.println("Created DTO: " + nuevaEntidadDTO);

        // 2. Persist it using the service
        System.out.println("\n2. Persisting entity...");
        EntidadDTO entidadGuardadaDTO = entidadServicio.crearEntidad(nuevaEntidadDTO);
        System.out.println("Persisted DTO: " + entidadGuardadaDTO);
        System.out.println("Generated ID: " + entidadGuardadaDTO.getId());

        // 3. Try to find one that doesn't exist
        System.out.println("\n3. Searching for non-existent entity (ID: 999)...");
        EntidadDTO noExiste = entidadServicio.buscarPorId(999);
        System.out.println("Result: " + (noExiste == null ? "NULL - Entity not found" : noExiste));

        // 4. Find the one we created (comes back as DTO)
        System.out.println("\n4. Searching for the created entity (ID: " + entidadGuardadaDTO.getId() + ")...");
        EntidadDTO encontradaDTO = entidadServicio.buscarPorId(1);
        System.out.println("Found DTO: " + encontradaDTO);

        // 5. Convert DTO back to entity and show in console
        if (encontradaDTO != null) {
            System.out.println("\n5. Converting DTO back to Entity...");
            Entidad entidadConvertida = new Entidad(encontradaDTO.getId(), encontradaDTO.getInfo());
            System.out.println("Final Entity: " + entidadConvertida);
            System.out.println("Entity ID: " + entidadConvertida.getId());
            System.out.println("Entity Info: " + entidadConvertida.getInfo());
        }

        System.out.println("\n=== TESTING COMPLETED ===");
    }
}