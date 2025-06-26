package app.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validador {
    /**
     * Mensaje personalizado para errores de validación
     */
    String mensaje() default "";
    
    /**
     * Si debe validar parámetros nulos
     */
    boolean validarNulos() default true;
    
    /**
     * Si debe validar IDs negativos
     */
    boolean validarIds() default true;
}
