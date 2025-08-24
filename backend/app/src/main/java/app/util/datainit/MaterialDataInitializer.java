package app.util.datainit;

import app.dtos.DTOMaterial;
import app.servicios.ServicioMaterial;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MaterialDataInitializer extends BaseDataInitializer {
    private static final int MATERIALS_TO_CREATE = 20;
    private final List<DTOMaterial> createdMaterials = new ArrayList<>();

    @Override
    public void initialize() {
        ServicioMaterial servicioMaterial = context.getBean(ServicioMaterial.class);
        
        System.out.println("Creating sample materials...");
        
        // Create various types of educational materials
        createDocumentMaterials(servicioMaterial);
        createImageMaterials(servicioMaterial);
        createVideoMaterials(servicioMaterial);
        createPresentationMaterials(servicioMaterial);
        
        System.out.println("Material creation completed. Total created: " + createdMaterials.size());
    }
    
    private void createDocumentMaterials(ServicioMaterial servicioMaterial) {
        String[] documentNames = {
            "Guía de Matemáticas Básicas",
            "Manual de Programación Java",
            "Apuntes de Física Cuántica",
            "Tutorial de Bases de Datos",
            "Documentación de Spring Boot",
            "Libro de Química Orgánica",
            "Guía de Diseño Web",
            "Manual de Inglés Empresarial"
        };
        
        String[] documentUrls = {
            "https://example.com/materials/matematicas-basicas.pdf",
            "https://example.com/materials/java-programming.pdf",
            "https://example.com/materials/fisica-cuantica.pdf",
            "https://example.com/materials/bases-datos.pdf",
            "https://example.com/materials/spring-boot-docs.pdf",
            "https://example.com/materials/quimica-organica.pdf",
            "https://example.com/materials/diseno-web.pdf",
            "https://example.com/materials/ingles-empresarial.pdf"
        };
        
        for (int i = 0; i < documentNames.length; i++) {
            try {
                DTOMaterial material = servicioMaterial.crearMaterial(documentNames[i], documentUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating document material: " + e.getMessage());
            }
        }
    }
    
    private void createImageMaterials(ServicioMaterial servicioMaterial) {
        String[] imageNames = {
            "Diagrama de Flujo de Datos",
            "Estructura de Base de Datos",
            "Arquitectura de Aplicación",
            "Diagrama UML de Clases",
            "Flujo de Autenticación",
            "Modelo de Datos",
            "Interfaz de Usuario",
            "Diagrama de Red"
        };
        
        String[] imageUrls = {
            "https://example.com/images/diagrama-flujo.png",
            "https://example.com/images/estructura-bd.jpg",
            "https://example.com/images/arquitectura-app.png",
            "https://example.com/images/uml-clases.jpg",
            "https://example.com/images/flujo-auth.png",
            "https://example.com/images/modelo-datos.jpg",
            "https://example.com/images/interfaz-ui.png",
            "https://example.com/images/diagrama-red.jpg"
        };
        
        for (int i = 0; i < imageNames.length; i++) {
            try {
                DTOMaterial material = servicioMaterial.crearMaterial(imageNames[i], imageUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating image material: " + e.getMessage());
            }
        }
    }
    
    private void createVideoMaterials(ServicioMaterial servicioMaterial) {
        String[] videoNames = {
            "Tutorial de Instalación",
            "Demostración de Funcionalidades",
            "Guía de Configuración",
            "Ejercicios Prácticos",
            "Explicación de Conceptos",
            "Resolución de Problemas",
            "Mejores Prácticas",
            "Casos de Uso Reales"
        };
        
        String[] videoUrls = {
            "https://example.com/videos/tutorial-instalacion.mp4",
            "https://example.com/videos/demo-funcionalidades.mp4",
            "https://example.com/videos/guia-configuracion.mp4",
            "https://example.com/videos/ejercicios-practicos.mp4",
            "https://example.com/videos/explicacion-conceptos.mp4",
            "https://example.com/videos/resolucion-problemas.mp4",
            "https://example.com/videos/mejores-practicas.mp4",
            "https://example.com/videos/casos-uso-reales.mp4"
        };
        
        for (int i = 0; i < videoNames.length; i++) {
            try {
                DTOMaterial material = servicioMaterial.crearMaterial(videoNames[i], videoUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating video material: " + e.getMessage());
            }
        }
    }
    
    private void createPresentationMaterials(ServicioMaterial servicioMaterial) {
        String[] presentationNames = {
            "Introducción al Curso",
            "Conceptos Fundamentales",
            "Metodología de Trabajo",
            "Evaluación y Criterios",
            "Recursos Adicionales",
            "Preguntas Frecuentes",
            "Calendario del Curso",
            "Contacto y Soporte"
        };
        
        String[] presentationUrls = {
            "https://example.com/presentations/introduccion-curso.pptx",
            "https://example.com/presentations/conceptos-fundamentales.pptx",
            "https://example.com/presentations/metodologia-trabajo.pptx",
            "https://example.com/presentations/evaluacion-criterios.pptx",
            "https://example.com/presentations/recursos-adicionales.pptx",
            "https://example.com/presentations/preguntas-frecuentes.pptx",
            "https://example.com/presentations/calendario-curso.pptx",
            "https://example.com/presentations/contacto-soporte.pptx"
        };
        
        for (int i = 0; i < presentationNames.length; i++) {
            try {
                DTOMaterial material = servicioMaterial.crearMaterial(presentationNames[i], presentationUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating presentation material: " + e.getMessage());
            }
        }
    }
    
    public List<DTOMaterial> getCreatedMaterials() {
        return createdMaterials;
    }
}
