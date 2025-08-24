package app.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private LocalDateTime enrollDate = LocalDateTime.now();
    
    @NotNull
    private boolean enrolled = false;
    
    // Lista de clases a las que está inscrito el alumno
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alumno_clases", joinColumns = @JoinColumn(name = "alumno_id"))
    @Column(name = "clase_id")
    private List<String> classIds = new ArrayList<>();
    
    // Lista de pagos realizados por el alumno
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alumno_pagos", joinColumns = @JoinColumn(name = "alumno_id"))
    @Column(name = "pago_id")
    private List<String> paymentIds = new ArrayList<>();
    
    // Lista de entregas de ejercicios del alumno
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alumno_entregas", joinColumns = @JoinColumn(name = "alumno_id"))
    @Column(name = "entrega_id")
    private List<String> submissionIds = new ArrayList<>();
    
    public Alumno() {
        super();
        this.setRole(Role.ALUMNO);
    }
    
    public Alumno(String username, String password, String firstName, String lastName,
                  String dni, String email, String phoneNumber) {
        super(username, password, firstName, lastName, dni, email, phoneNumber);
        this.setRole(Role.ALUMNO);
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
     * @param classId ID de la clase
     */
    public void agregarClase(String classId) {
        if (!this.classIds.contains(classId)) {
            this.classIds.add(classId);
        }
    }
    
    /**
     * Remueve una clase del alumno
     * @param classId ID de la clase
     */
    public void removerClase(String classId) {
        this.classIds.remove(classId);
    }
    
    /**
     * Agrega un pago al alumno
     * @param paymentId ID del pago
     */
    public void agregarPago(String paymentId) {
        if (!this.paymentIds.contains(paymentId)) {
            this.paymentIds.add(paymentId);
        }
    }
    
    /**
     * Remueve un pago del alumno
     * @param paymentId ID del pago
     */
    public void removerPago(String paymentId) {
        this.paymentIds.remove(paymentId);
    }
    
    /**
     * Agrega una entrega de ejercicio al alumno
     * @param submissionId ID de la entrega
     */
    public void agregarEntrega(String submissionId) {
        if (!this.submissionIds.contains(submissionId)) {
            this.submissionIds.add(submissionId);
        }
    }
    
    /**
     * Remueve una entrega de ejercicio del alumno
     * @param submissionId ID de la entrega
     */
    public void removerEntrega(String submissionId) {
        this.submissionIds.remove(submissionId);
    }
    
    /**
     * Verifica si el alumno está inscrito en una clase específica
     * @param classId ID de la clase
     * @return true si está inscrito, false en caso contrario
     */
    public boolean estaInscritoEnClase(String classId) {
        return this.classIds.contains(classId);
    }
    
    /**
     * Verifica si el alumno tiene una entrega específica
     * @param submissionId ID de la entrega
     * @return true si tiene la entrega, false en caso contrario
     */
    public boolean tieneEntrega(String submissionId) {
        return this.submissionIds.contains(submissionId);
    }
}