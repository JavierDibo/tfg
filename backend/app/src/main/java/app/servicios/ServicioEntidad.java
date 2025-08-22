package app.servicios;

import app.excepciones.EntidadNoEncontradaException;
import app.dtos.DTOEntidad;
import app.dtos.DTOParametrosBusqueda;
import app.entidades.Entidad;
import app.repositorios.RepositorioEntidad;
import app.util.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioEntidad {

    private final RepositorioEntidad repositorioEntidad;

    public List<DTOEntidad> obtenerTodasLasEntidades() {
        return repositorioEntidad.findAllOrderedById()
                .stream()
                .map(DTOEntidad::new)
                .toList();
    }

    public DTOEntidad obtenerEntidadPorId(int id) {
        Entidad entidad = repositorioEntidad.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entidad, "Entidad", "ID", id);
        return new DTOEntidad(entidad);
    }

    public List<DTOEntidad> obtenerEntidadesPorInfo(String info) {
        return repositorioEntidad.findByInfoContainingIgnoreCase(info)
                .stream()
                .map(DTOEntidad::new)
                .toList();
    }

    public List<DTOEntidad> obtenerEntidadesPorOtraInfo(String otraInfo) {
        return repositorioEntidad.findByOtraInfoContainingIgnoreCase(otraInfo)
                .stream()
                .map(DTOEntidad::new)
                .toList();
    }

    public List<DTOEntidad> obtenerEntidadesPorParametros(DTOParametrosBusqueda parametros) {
        // Si ambos parámetros son nulos, devolver todas las entidades
        if (parametros.info() == null && parametros.otraInfo() == null) {
            return obtenerTodasLasEntidades();
        }
        
        return repositorioEntidad.findByInfoAndOtraInfoContainingIgnoreCase(
                parametros.info(), parametros.otraInfo())
                .stream()
                .map(DTOEntidad::new)
                .toList();
    }

    public DTOEntidad crearEntidad(DTOEntidad dtoEntidad) {
        Entidad entidad = new Entidad(dtoEntidad);
        Entidad entidadGuardada = repositorioEntidad.save(entidad);
        return new DTOEntidad(entidadGuardada);
    }

    public DTOEntidad actualizarEntidad(int id, DTOEntidad dtoParcial) {
        Entidad entidad = repositorioEntidad.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entidad, "Entidad", "ID", id);

        if (dtoParcial.info() != null) {
            entidad.setInfo(dtoParcial.info());
        }

        if (dtoParcial.otraInfo() != null) {
            entidad.setOtraInfo(dtoParcial.otraInfo());
        }

        Entidad entidadActualizada = repositorioEntidad.save(entidad);
        return new DTOEntidad(entidadActualizada);
    }

    public List<DTOEntidad> borrarTodasLasEntidades() {
        List<Entidad> entidades = repositorioEntidad.findAllOrderedById();
        repositorioEntidad.deleteAll();
        return entidades.stream()
                .map(DTOEntidad::new)
                .toList();
    }

    public DTOEntidad borrarEntidadPorId(int id) {
        Entidad entidad = repositorioEntidad.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entidad, "Entidad", "ID", id);
        repositorioEntidad.deleteById(id);
        return new DTOEntidad(entidad);
    }
}