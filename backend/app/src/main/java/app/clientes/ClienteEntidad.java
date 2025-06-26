package app.clientes;

import app.aop.Validador;
import app.servicios.ServicioEntidad;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClienteEntidad {

    @Autowired
    ServicioEntidad entidadServicio;

    @Validador
    public void ejecutarOperacion(int id, String texto) {
        log.info("Ejecutando ClienteEntidad con id: {} y texto: {}", id, texto);
        // Aquí iría la lógica de negocio
        // El aspecto ya habrá validado los parámetros antes de llegar aquí
    }
    
    @Validador(validarIds = false) // Ejemplo de configuración personalizada
    public void operacionSinValidarId(int numero, String texto) {
        log.info("Ejecutando operación sin validar ID: {} y texto: {}", numero, texto);
        // En este caso, no se validará que 'numero' sea positivo
    }
}