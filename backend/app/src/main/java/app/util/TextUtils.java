package app.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utilidad para normalización de texto y búsquedas insensibles a acentos y mayúsculas
 */
public class TextUtils {
    
    // Patrón para eliminar signos diacríticos (acentos, tildes, etc.)
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    /**
     * Normaliza un texto eliminando acentos y convirtiéndolo a minúsculas
     * @param texto El texto a normalizar
     * @return El texto normalizado, o null si el input es null
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        
        // Convertir a minúsculas
        String textoMinusculas = texto.toLowerCase();
        
        // Normalizar para separar caracteres base de diacríticos
        String normalizado = Normalizer.normalize(textoMinusculas, Normalizer.Form.NFD);
        
        // Eliminar diacríticos
        String sinAcentos = DIACRITICS_PATTERN.matcher(normalizado).replaceAll("");
        
        return sinAcentos.trim();
    }
    
    /**
     * Verifica si un texto contiene otro texto, ignorando acentos y mayúsculas
     * @param texto El texto donde buscar
     * @param busqueda El texto a buscar
     * @return true si el texto contiene la búsqueda
     */
    public static boolean contiene(String texto, String busqueda) {
        if (texto == null || busqueda == null) {
            return false;
        }
        
        String textoNormalizado = normalizar(texto);
        String busquedaNormalizada = normalizar(busqueda);
        
        return textoNormalizado.contains(busquedaNormalizada);
    }
    
    /**
     * Verifica si dos textos son iguales, ignorando acentos y mayúsculas
     * @param texto1 El primer texto
     * @param texto2 El segundo texto
     * @return true si los textos son equivalentes
     */
    public static boolean sonIguales(String texto1, String texto2) {
        if (texto1 == null && texto2 == null) {
            return true;
        }
        
        if (texto1 == null || texto2 == null) {
            return false;
        }
        
        String texto1Normalizado = normalizar(texto1);
        String texto2Normalizado = normalizar(texto2);
        
        return texto1Normalizado.equals(texto2Normalizado);
    }
    
    /**
     * Prepara un término de búsqueda para usar en queries LIKE con comodines
     * @param termino El término a preparar
     * @return El término normalizado listo para LIKE
     */
    public static String prepararParaLike(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return null;
        }
        
        return normalizar(termino);
    }
}
