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
 * Student Entity
 * Represents a student in the platform
 * Based on UML specification
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ALUMNO")
public class Alumno extends Usuario {
    
    @NotNull
    @Column(name = "enrollment_date")
    private LocalDateTime enrollDate = LocalDateTime.now();
    
    @NotNull
    private boolean enrolled = false;
    
    // List of classes the student is enrolled in
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "student_classes", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "class_id")
    private List<String> classIds = new ArrayList<>();
    
    // List of payments made by the student
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "student_payments", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "payment_id")
    private List<String> paymentIds = new ArrayList<>();
    
    // List of exercise submissions by the student
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "student_submissions", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "submission_id")
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
     * Method to reset password according to UML
     * TODO: Implement email-based reset logic
     */
    public void resetPassword() {
        // TODO: Implement according to project specifications
        throw new UnsupportedOperationException("resetPassword method not implemented");
    }
    
    /**
     * Adds a class to the student
     * @param classId ID of the class
     */
    public void addClass(String classId) {
        if (!this.classIds.contains(classId)) {
            this.classIds.add(classId);
        }
    }
    
    /**
     * Removes a class from the student
     * @param classId ID of the class
     */
    public void removeClass(String classId) {
        this.classIds.remove(classId);
    }
    
    /**
     * Adds a payment to the student
     * @param paymentId ID of the payment
     */
    public void addPayment(String paymentId) {
        if (!this.paymentIds.contains(paymentId)) {
            this.paymentIds.add(paymentId);
        }
    }
    
    /**
     * Removes a payment from the student
     * @param paymentId ID of the payment
     */
    public void removePayment(String paymentId) {
        this.paymentIds.remove(paymentId);
    }
    
    /**
     * Adds an exercise submission to the student
     * @param submissionId ID of the submission
     */
    public void addSubmission(String submissionId) {
        if (!this.submissionIds.contains(submissionId)) {
            this.submissionIds.add(submissionId);
        }
    }
    
    /**
     * Removes an exercise submission from the student
     * @param submissionId ID of the submission
     */
    public void removeSubmission(String submissionId) {
        this.submissionIds.remove(submissionId);
    }
    
    /**
     * Checks if the student is enrolled in a specific class
     * @param classId ID of the class
     * @return true if enrolled, false otherwise
     */
    public boolean isEnrolledInClass(String classId) {
        return this.classIds.contains(classId);
    }
    
    /**
     * Checks if the student has a specific submission
     * @param submissionId ID of the submission
     * @return true if has the submission, false otherwise
     */
    public boolean hasSubmission(String submissionId) {
        return this.submissionIds.contains(submissionId);
    }
    
}