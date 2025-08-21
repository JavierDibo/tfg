package app.util.datainit;

import app.dtos.DTOAlumno;
import app.dtos.DTOCurso;
import app.servicios.ServicioAlumno;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class StudentEnrollmentInitializer extends BaseDataInitializer {
    private static final double ENROLLMENT_PROBABILITY = 0.7; // 70% chance a student enrolls in a course
    private static final int MAX_COURSES_PER_STUDENT = 5; // Maximum courses a student can enroll in
    private final Random random = new Random();

    @Override
    public void initialize() {
        ServicioAlumno servicioAlumno = context.getBean(ServicioAlumno.class);
        StudentDataInitializer studentInit = context.getBean(StudentDataInitializer.class);
        CourseDataInitializer courseInit = context.getBean(CourseDataInitializer.class);
        
        List<DTOAlumno> students = studentInit.getCreatedStudents();
        List<DTOCurso> courses = courseInit.getCreatedCourses();
        
        if (students.isEmpty() || courses.isEmpty()) {
            System.out.println("No students or courses available for enrollment");
            return;
        }
        
        System.out.println("Enrolling students in courses...");
        System.out.println("Students: " + students.size() + ", Courses: " + courses.size());
        
        int totalEnrollments = 0;
        
        // For each student, randomly decide which courses to enroll in
        for (DTOAlumno student : students) {
            // Shuffle courses to randomize selection
            List<DTOCurso> shuffledCourses = new ArrayList<>(courses);
            Collections.shuffle(shuffledCourses, random);
            
            int enrolledInThisStudent = 0;
            
            for (DTOCurso course : shuffledCourses) {
                // Stop if student has reached max courses
                if (enrolledInThisStudent >= MAX_COURSES_PER_STUDENT) {
                    break;
                }
                
                // Randomly decide if student enrolls in this course
                if (random.nextDouble() < ENROLLMENT_PROBABILITY) {
                    try {
                        servicioAlumno.inscribirEnClase(student.id(), course.id());
                        totalEnrollments++;
                        enrolledInThisStudent++;
                        System.out.println("✓ Enrolled student " + student.nombre() + " in course " + course.titulo());
                    } catch (Exception e) {
                        // If student is already enrolled, that's fine (rule 2)
                        if (e.getMessage().contains("ya está inscrito")) {
                            System.out.println("ℹ Student " + student.nombre() + " already enrolled in " + course.titulo());
                        } else {
                            System.err.println("✗ Error enrolling student " + student.nombre() + " in course " + course.titulo() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        System.out.println("✓ Enrollment completed. Total enrollments: " + totalEnrollments);
        
        // Print enrollment statistics
        printEnrollmentStatistics(students, courses, servicioAlumno);
    }
    
    private void printEnrollmentStatistics(List<DTOAlumno> students, List<DTOCurso> courses, ServicioAlumno servicioAlumno) {
        System.out.println("\n=== Enrollment Statistics ===");
        
        // Count students per course
        for (DTOCurso course : courses) {
            try {
                int studentCount = servicioAlumno.obtenerAlumnosPorClasePaginados(course.id(), 0, 1000, "id", "ASC").content().size();
                System.out.println("Course: " + course.titulo() + " - Students: " + studentCount);
            } catch (Exception e) {
                System.err.println("Error getting students for course " + course.titulo() + ": " + e.getMessage());
            }
        }
        
        // Count courses per student
        int studentsWithCourses = 0;
        int totalStudentCourses = 0;
        
        for (DTOAlumno student : students) {
            try {
                int courseCount = servicioAlumno.obtenerClasesPorAlumno(student.id()).size();
                if (courseCount > 0) {
                    studentsWithCourses++;
                    totalStudentCourses += courseCount;
                }
            } catch (Exception e) {
                System.err.println("Error getting courses for student " + student.nombre() + ": " + e.getMessage());
            }
        }
        
        System.out.println("\nStudents with courses: " + studentsWithCourses + "/" + students.size());
        if (studentsWithCourses > 0) {
            System.out.println("Average courses per enrolled student: " + 
                String.format("%.2f", (double) totalStudentCourses / studentsWithCourses));
        }
    }
}
