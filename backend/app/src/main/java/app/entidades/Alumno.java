package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Alumno
 * Representa a un alumno en la plataforma
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ALUMNO")
public class Alumno extends Usuario {
    
    @NotNull
    @Column(name = "fecha_inscripcion")
    private LocalDateTime fechaInscripcion = LocalDateTime.now();
    
    @NotNull
    private boolean matriculado = false;
    
    // Lista de clases a las que está inscrito el alumno
    @ElementCollection
    @CollectionTable(name = "alumno_clases", joinColumns = @JoinColumn(name = "alumno_id"))
    @Column(name = "clase_id")
    private List<String> clasesId = new ArrayList<>();
    
    // Lista de pagos realizados por el alumno
    @ElementCollection
    @CollectionTable(name = "alumno_pagos", joinColumns = @JoinColumn(name = "alumno_id"))
    @Column(name = "pago_id")
    private List<String> pagosId = new ArrayList<>();
    
    // Lista de entregas de ejercicios del alumno
    @ElementCollection
    @CollectionTable(name = "alumno_entregas", joinColumns = @JoinColumn(name = "alumno_id"))
    @Column(name = "entrega_id")
    private List<String> entregasId = new ArrayList<>();
    
    public Alumno() {
        super();
        this.setRol(Rol.ALUMNO);
    }
    
    public Alumno(String usuario, String password, String nombre, String apellidos, 
                  String dni, String email, String numeroTelefono) {
        super(usuario, password, nombre, apellidos, dni, email, numeroTelefono);
        this.setRol(Rol.ALUMNO);
    }
    
    /**
     * metodo para resetear password según UML
     * TODO: Implementar lógica de reseteo por email
     */
    public void resetearpassword() {
        // TODO: Implementar según especificaciones del proyecto
        throw new UnsupportedOperationException("metodo resetearpassword por implementar");
    }
    
    /**
     * Agrega una clase al alumno
     * @param claseId ID de la clase
     */
    public void agregarClase(String claseId) {
        if (!this.clasesId.contains(claseId)) {
            this.clasesId.add(claseId);
        }
    }
    
    /**
     * Remueve una clase del alumno
     * @param claseId ID de la clase
     */
    public void removerClase(String claseId) {
        this.clasesId.remove(claseId);
    }
    
    /**
     * Agrega un pago al alumno
     * @param pagoId ID del pago
     */
    public void agregarPago(String pagoId) {
        if (!this.pagosId.contains(pagoId)) {
            this.pagosId.add(pagoId);
        }
    }
    
    /**
     * Remueve un pago del alumno
     * @param pagoId ID del pago
     */
    public void removerPago(String pagoId) {
        this.pagosId.remove(pagoId);
    }
    
    /**
     * Agrega una entrega de ejercicio al alumno
     * @param entregaId ID de la entrega
     */
    public void agregarEntrega(String entregaId) {
        if (!this.entregasId.contains(entregaId)) {
            this.entregasId.add(entregaId);
        }
    }
    
    /**
     * Remueve una entrega de ejercicio del alumno
     * @param entregaId ID de la entrega
     */
    public void removerEntrega(String entregaId) {
        this.entregasId.remove(entregaId);
    }
    
    /**
     * Verifica si el alumno está inscrito en una clase específica
     * @param claseId ID de la clase
     * @return true si está inscrito, false en caso contrario
     */
    public boolean estaInscritoEnClase(String claseId) {
        return this.clasesId.contains(claseId);
    }
    
    /**
     * Verifica si el alumno tiene una entrega específica
     * @param entregaId ID de la entrega
     * @return true si tiene la entrega, false en caso contrario
     */
    public boolean tieneEntrega(String entregaId) {
        return this.entregasId.contains(entregaId);
    }
}