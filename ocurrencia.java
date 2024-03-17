import java.io.*;
import java.util.*;
import java.text.Normalizer;
import java.util.regex.Pattern;


public class Ocurrencia implements Serializable{ //clase que representa la ocurrencia de un término en el pc

    private TreeMap<String, Integer> archivos; //frecuencia término por archivo
    private int frecuencia; //frecuencia total del término

    public Ocurrencia(String archivo) { //constructor
        archivos = new TreeMap<String, Integer>();
        frecuencia = 0;
        addOcurrencia(archivo);
    }

    
    public void addOcurrencia(String archivo) { //añade una ocurrencia del término en el archivo
        Integer cont = archivos.get(archivo);
        if (cont == null) archivos.put(archivo, new Integer(1));
        else archivos.put(archivo, new Integer(cont.intValue() + 1));
        frecuencia++;
    }

    public String getArchivos() { //devuelve la lista de archivos en los que aparece el término
        String lista = "";
        for (String archivo : archivos.keySet()) {
            lista += archivos.get(archivo) + " veces en " + archivo + "\n";
        }
        return lista;
    }
    public int getFrecuencia() { //devuelve la frecuencia total del término
        return frecuencia;
    }

     public void setFrecuencia(int frec) { //establece la frecuencia total del término
        frecuencia = frec;
    }

    public int getURL(String archivo) { //devuelve la frecuencia del término en el archivo
        Integer cont = archivos.get(archivo);
        if (cont == null) return 0;
        else return cont.intValue();
    }

    public void setURL(String archivo, int frec) { //establece la frecuencia del término en el archivo
        archivos.put(archivo, new Integer(frec));
    }   
    
}
