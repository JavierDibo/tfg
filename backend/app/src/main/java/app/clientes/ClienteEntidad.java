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

    private void crearYRecuperarEntidad() {
        // Crear una entidad
        Entidad entidad = new Entidad("holii");

        // Guardar la entidad
        Entidad entidadGuardada = entidadServicio.crearEntidad(entidad);

        // Obtener la entidad por ID
        Optional<Entidad> optEntidadObtenida = entidadServicio.buscarPorId(entidadGuardada.getId());
        if (optEntidadObtenida.isEmpty()) {
            System.out.println("Entidad no encontrada");
            return;
        }

        Entidad entidadObtenida = optEntidadObtenida.get();
        System.out.println("Entidad obtenida: " + entidadObtenida.getId() + ", " + entidadObtenida.getInfo());
    }

    private void borrarTodasLasEntidades() {
        List<Entidad> entidades = entidadServicio.devolverTodasLasEntidades();
        if (entidades.isEmpty()) {
            System.out.println("No hay entidades para borrar");
            return;
        }

        System.out.println("Se van a borrar: " + entidades.size() + " entidades: ");
        for (Entidad entidad : entidades) {
            System.out.println(entidad.getId() + ", " + entidad.getInfo());
        }
        entidadServicio.borrarTodasLasEntidades(entidades);
        System.out.println("Todas las entidades han sido borradas");
    }

    public void run() {
        // borrarTodasLasEntidades();
    }
}