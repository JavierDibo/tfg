package app.clientes;

import app.entidades.Entidad;
import app.servicios.ServicioEntidad;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

public class ClienteEntidad {

    ApplicationContext appContext;
    ServicioEntidad entidadServicio;

    public ClienteEntidad(ApplicationContext appContext) {
        this.appContext = appContext;
        entidadServicio = appContext.getBean(ServicioEntidad.class);
    }

    public void run() {
        // borrarTodasLasEntidades();
    }
}