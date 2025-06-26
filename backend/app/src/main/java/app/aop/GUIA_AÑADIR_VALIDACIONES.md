# 🛠️ Guía: Cómo Añadir Nuevas Validaciones

**Para referencia futura: Pasos exactos para extender el sistema de validación AOP**

---

## 📋 **Lista de Archivos a Tocar (SOLO 2)**

1. `📄 src/main/java/app/aop/Validador.java` - Añadir configuración
2. `📄 src/main/java/app/aop/MiAspecto.java` - Añadir lógica de validación

---

## 🎯 **PASO 1: Configurar en `Validador.java`**

**QUÉ HACER:** Añadir un `boolean` para activar/desactivar tu nueva validación

```java
public @interface Validador {
    // ... código existente ...
    
    // ✨ AÑADIR LÍNEA para tu nueva validación
    boolean validarEmails() default true;          // Ejemplo: emails
    boolean validarTelefonos() default true;       // Ejemplo: teléfonos  
    boolean validarUrls() default true;            // Ejemplo: URLs
}
```

**PATRÓN:** `boolean validar[TuTipo]() default true;`

---

## 🔧 **PASO 2: Implementar en `MiAspecto.java`**

### **2.1 Localizar el método `validarTipo()`**
```java
private void validarTipo(String paramName, Object arg, Validador validador) {
    // ... código existente ...
    
    if (arg instanceof String) {
        String texto = (String) arg;
        
        // Validaciones existentes...
        
        // ✨ AÑADIR TU NUEVA VALIDACIÓN AQUÍ
        if (validador.validarEmails() && esParametroEmail(paramName)) {
            if (!esEmailValido(texto)) {
                throw new ParametroInvalidoException(paramName, texto, "no es un email válido");
            }
        }
    }
}
```

### **2.2 Añadir métodos auxiliares al final de la clase**
```java
// ✨ AÑADIR estos métodos al final de MiAspecto.java

// Detectar si el parámetro debe validarse como email
private boolean esParametroEmail(String paramName) {
    String lower = paramName.toLowerCase();
    return lower.contains("email") || lower.contains("mail");
}

// Validar el formato del email
private boolean esEmailValido(String email) {
    String patron = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(patron);
}
```

---

## 📚 **PATRONES de Validación Comunes**

### **🔗 URLs**
```java
// En validarTipo():
if (validador.validarUrls() && esParametroUrl(paramName)) {
    if (!esUrlValida(texto)) {
        throw new ParametroInvalidoException(paramName, texto, "no es una URL válida");
    }
}

// Métodos auxiliares:
private boolean esParametroUrl(String paramName) {
    return paramName.toLowerCase().contains("url");
}

private boolean esUrlValida(String url) {
    String patron = "^https?://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(/.*)?$";
    return url.matches(patron);
}
```

### **📞 Teléfonos**
```java
// En validarTipo():
if (validador.validarTelefonos() && esParametroTelefono(paramName)) {
    if (!esTelefonoValido(texto)) {
        throw new ParametroInvalidoException(paramName, texto, "no es un teléfono válido");
    }
}

// Métodos auxiliares:
private boolean esParametroTelefono(String paramName) {
    String lower = paramName.toLowerCase();
    return lower.contains("telefono") || lower.contains("phone") || lower.contains("movil");
}

private boolean esTelefonoValido(String telefono) {
    String patron = "^[+]?[0-9]{9,15}$";  // Permite + y 9-15 dígitos
    return telefono.replaceAll("[-\\s]", "").matches(patron);
}
```

### **📅 Fechas (formato específico)**
```java
// En validarTipo():
if (validador.validarFechas() && esParametroFecha(paramName)) {
    if (!esFechaValida(texto)) {
        throw new ParametroInvalidoException(paramName, texto, "debe tener formato dd/MM/yyyy");
    }
}

// Métodos auxiliares:
private boolean esParametroFecha(String paramName) {
    return paramName.toLowerCase().contains("fecha");
}

private boolean esFechaValida(String fecha) {
    String patron = "^\\d{2}/\\d{2}/\\d{4}$";
    return fecha.matches(patron);
}
```

---

## ✅ **EJEMPLO COMPLETO: Añadir Validación de URLs**

### **1. En `Validador.java`:**
```java
boolean validarUrls() default true;
```

### **2. En `MiAspecto.java` (método `validarTipo`):**
```java
// Dentro del if (arg instanceof String)
if (validador.validarUrls() && esParametroUrl(paramName)) {
    if (!esUrlValida(texto)) {
        throw new ParametroInvalidoException(paramName, texto, "no es una URL válida");
    }
}
```

### **3. En `MiAspecto.java` (al final de la clase):**
```java
private boolean esParametroUrl(String paramName) {
    return paramName.toLowerCase().contains("url");
}

private boolean esUrlValida(String url) {
    String patron = "^https?://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(/.*)?$";
    return url.matches(patron);
}
```

### **4. Usar en tu Controller:**
```java
@Validador
@PostMapping("/añadir-sitio")
public ResponseEntity<?> añadirSitio(@RequestParam String urlSitio) {
    // urlSitio se valida automáticamente ✅
}
```

---

## 🎮 **USO con Configuración Personalizada**

```java
@Validador(validarUrls = false)  // Deshabilitar URLs para este método
@PostMapping("/temporal")
public ResponseEntity<?> operacionTemporal(@RequestParam String urlTemporal) {
    // No se validará la URL
}
```

---

## 🔄 **Flujo de Desarrollo**

1. **Identificar** qué quieres validar (email, teléfono, etc.)
2. **Tocar `Validador.java`**: Añadir `boolean validar[Tipo]() default true;`
3. **Tocar `MiAspecto.java`**: 
   - Añadir `if (validador.validar[Tipo]() && es[Tipo](paramName))`
   - Añadir métodos `es[Tipo]()` y `es[Tipo]Valido()`
4. **Usar normalmente**: El sistema detecta automáticamente por nombre del parámetro

---

## 🏆 **¡Listo!**

Con estos 2 archivos tocados, tienes validación automática en toda tu API. 
**El sistema es extensible infinitamente siguiendo este patrón.** 