package app.aop;

import app.aop.exceptions.ParametroInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
public class MiAspecto {

    @Around("@annotation(validador)")
    public Object validarMetodo(ProceedingJoinPoint joinPoint, Validador validador) throws Throwable {
        log.debug("Ejecutando validación para método: {}", joinPoint.getSignature().getName());
        
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();
        
        // Validar parámetros
        validarParametros(args, parameters, validador);
        
        // Si todas las validaciones pasan, ejecutar el método original
        Object resultado = joinPoint.proceed();
        
        log.debug("Validación exitosa para método: {}", joinPoint.getSignature().getName());
        return resultado;
    }
    
    @Around("@within(validador) && !@annotation(app.aop.Validador)")
    public Object validarClase(ProceedingJoinPoint joinPoint, Validador validador) throws Throwable {
        // Solo aplicar validación a nivel de clase si el método no tiene su propia anotación @Validador
        return validarMetodo(joinPoint, validador);
    }
    
    private void validarParametros(Object[] args, Parameter[] parameters, Validador validador) {
        for (int i = 0; i < args.length && i < parameters.length; i++) {
            Object arg = args[i];
            Parameter parameter = parameters[i];
            String paramName = parameter.getName();
            
            // Validar nulos si está habilitado
            if (validador.validarNulos() && arg == null) {
                throw new ParametroInvalidoException(paramName, null, "no puede ser nulo");
            }
            
            // Si el argumento no es nulo, continuar con otras validaciones
            if (arg != null) {
                validarTipo(paramName, arg, validador);
            }
        }
    }
    
    private void validarTipo(String paramName, Object arg, Validador validador) {
        // Validar IDs (parámetros int que representen IDs)
        if (validador.validarIds() && arg instanceof Integer) {
            int valor = (Integer) arg;
            if (paramName.toLowerCase().contains("id") && valor < 1) {
                throw new ParametroInvalidoException(paramName, valor, "debe ser mayor o igual a 1");
            }
        }
        
        // Validar Strings
        if (arg instanceof String) {
            String texto = (String) arg;
            if (texto.isBlank()) {
                throw new ParametroInvalidoException(paramName, texto, "no puede estar vacío");
            }
            if (texto.length() > 100) {
                throw new ParametroInvalidoException(paramName, texto, "no puede tener más de 100 caracteres");
            }
        }
    }
}
