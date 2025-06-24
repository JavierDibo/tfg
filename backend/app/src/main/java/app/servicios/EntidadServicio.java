package app.servicios;

import app.dtos.EntidadDTO;
import app.entidades.Entidad;
import app.repositorios.EntidadRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntidadServicio {

    @Autowired
    private EntidadRepositorio entidadRepositorio;

    // Crear una nueva entidad
    public EntidadDTO crearEntidad(EntidadDTO entidadDTO) {
        Entidad entidad = convertirDTOAEntidad(entidadDTO);
        Entidad entidadGuardada = entidadRepositorio.save(entidad);
        return convertirEntidadADTO(entidadGuardada);
    }

    // Buscar entidad por ID
    public EntidadDTO buscarPorId(int id) {
        Optional<Entidad> entidadOpt = entidadRepositorio.findById(id);
        if (entidadOpt.isPresent()) {
            return convertirEntidadADTO(entidadOpt.get());
        }
        return null;
    }

    // Actualizar una entidad existente
    public EntidadDTO actualizarEntidad(int id, EntidadDTO entidadDTO) {
        Optional<Entidad> entidadExistenteOpt = entidadRepositorio.findById(id);
        if (entidadExistenteOpt.isPresent()) {
            Entidad entidadExistente = entidadExistenteOpt.get();
            entidadExistente.setInfo(entidadDTO.getInfo());
            Entidad entidadActualizada = entidadRepositorio.save(entidadExistente);
            return convertirEntidadADTO(entidadActualizada);
        }
        return null;
    }

    // Métodos de conversión entre Entity y DTO
    private EntidadDTO convertirEntidadADTO(Entidad entidad) {
        return new EntidadDTO(entidad.getId(), entidad.getInfo());
    }

    private Entidad convertirDTOAEntidad(EntidadDTO entidadDTO) {
        Entidad entidad = new Entidad();
        entidad.setId(entidadDTO.getId() != null ? entidadDTO.getId() : 0);
        entidad.setInfo(entidadDTO.getInfo());
        return entidad;
    }
} 