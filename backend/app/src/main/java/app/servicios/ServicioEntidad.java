package app.servicios;

import app.aop.Validador;
import app.aop.exceptions.EntidadNoEncontradaException;
import app.dtos.DTOEntidad;
import app.entidades.Entidad;
import app.repositorios.RepositorioEntidad;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ServicioEntidad {

    @Autowired
    private RepositorioEntidad repositorioEntidad;

    private DTOEntidad ADTO(Entidad entidad) {
        return new DTOEntidad(entidad);
    }

    private List<DTOEntidad> ADTO(List<Entidad> entidades) {
        return entidades
                .stream()
                .map(DTOEntidad::new)
                .toList();
    }

    private Entidad AEntidad(DTOEntidad dtoEntidad) {
        return new Entidad(dtoEntidad);
    }

    private List<Entidad> AEntidad(List<DTOEntidad> dtos) {
        return dtos
                .stream()
                .map(Entidad::new)
                .toList();
    }

    public List<Entidad> obtenerTodasLasEntidades() {
        List<Entidad> entidades = repositorioEntidad.obtenerTodasLasEntidades();
        if (entidades.isEmpty()) {
            return List.of();
        }
        return entidades;
    }

    public List<DTOEntidad> obtenerTodasLasEntidadesDTO() {
        List<Entidad> entidades = obtenerTodasLasEntidades();
        return ADTO(entidades);
    }

    @Validador
    public Entidad obtenerEntidadPorId(int id) {
        return repositorioEntidad.obtenerEntidadPorId(id);
    }

    @Validador
    public DTOEntidad obtenerEntidadPorIdDTO(int id) {
        Entidad entidad = obtenerEntidadPorId(id);
        return ADTO(entidad);
    }

    @Validador
    public List<Entidad> obtenerEntidadesPorInfo(String info) {
        List<Entidad> entidades = repositorioEntidad.obtenerEntidadesPorInfo(info);
        if (entidades.isEmpty())
            return List.of();
        else
            return entidades;
    }

    public List<DTOEntidad> obtenerEntidadesPorInfoDTO(String info) {
        List<Entidad> entidades = obtenerEntidadesPorInfo(info);
        return ADTO(entidades);
    }

    public Entidad crearEntidad(@NotNull Entidad entidad) {
        return repositorioEntidad.crearEntidad(entidad);
    }

    public DTOEntidad crearEntidadDTO(@NotNull DTOEntidad dtoEntidad) {
        Entidad entidad = AEntidad(dtoEntidad);
        return ADTO(crearEntidad(entidad));
    }

    @Validador
    public Entidad actualizarEntidad(int id, DTOEntidad dtoParcial) {
        Entidad entidad = repositorioEntidad.obtenerEntidadPorId(id);

        if (dtoParcial.info() != null)
            entidad.setInfo(dtoParcial.info());

        if (dtoParcial.otraInfo() != null)
            entidad.setInfo(dtoParcial.otraInfo());

        return repositorioEntidad.actualizarEntidad(entidad);
    }

    public DTOEntidad actualizarEntidadDTO(int id, DTOEntidad dtoParcial) {
        return ADTO(actualizarEntidad(id, dtoParcial));
    }

    public List<Entidad> borrarTodasLasEntidades() {
        List<Entidad> entidades = repositorioEntidad.borrarTodasLasEntidades();
        if (entidades.isEmpty())
            return List.of();
        else
            return entidades;
    }

    public List<DTOEntidad> borrarTodasLasEntidadesDTO() {
        return ADTO(borrarTodasLasEntidades());
    }

    @Validador
    public Entidad borrarEntidadPorId(int id) {
        return repositorioEntidad.borrarEntidadPorId(id);
    }

    @Validador
    public DTOEntidad borrarEntidadPorIdDTO(int id) {
        return ADTO(borrarEntidadPorId(id));
    }
}