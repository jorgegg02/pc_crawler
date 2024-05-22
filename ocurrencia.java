import java.io.*;
import java.util.*;




public class Ocurrencia implements Serializable{ //clase que representa la ocurrencia de un término en el pc

    private TreeMap<Integer, Integer> archivos; //frecuencia término por archivo
    private int frecuencia; //frecuencia total del término

    public Ocurrencia(Integer idArchivo) { //constructor
        archivos = new TreeMap<Integer, Integer>();
        frecuencia = 0;
        addOcurrencia(idArchivo);
    }

    
    public void addOcurrencia(Integer idArchivo) { //añade una ocurrencia del término en el archivo
        Integer cont = archivos.get(idArchivo);
        if (cont == null) archivos.put(idArchivo, 1);
        else archivos.put(idArchivo, cont.intValue() + 1);
        frecuencia++;
    }

    // devuelve una lista de arreglos de enteros, donde cada arreglo tiene dos elementos: el id del archivo y la frecuencia del término en ese archivo
    public List<Integer[]> getArchivos() { 
        TreeMap<Integer, ArrayList<Integer>> listaOrdenada = new TreeMap<Integer, ArrayList<Integer>>();
        //Creamos un treemap, la llave es la frecuencia de un término en un archivo y el valor es una lista de los archivos que tienen esa frecuencia
        for (Integer archivo : archivos.keySet() ) {
            Integer frencuenciaPorArchivo = archivos.get(archivo);
            
            ArrayList<Integer> listaTerminosKveces = listaOrdenada.get(frencuenciaPorArchivo);
            if (listaTerminosKveces == null) {
                listaTerminosKveces = new ArrayList<Integer>();
                listaTerminosKveces.add(archivo);
                listaOrdenada.put(frencuenciaPorArchivo, listaTerminosKveces);
            }
            else {
                listaTerminosKveces.add(archivo);
            }   
            
        }

        List<Integer[]> listaArchivos = new ArrayList<Integer[]>();
        //Para cada frecuencia (se recorre de mayor a menos) creamos la tupla [idArchivo, frecuencia]
        //Cada frecuencia puede tener varios archivos, es decir, varios archivos tienen la misma frecuencia
        for (Integer frecuencia : listaOrdenada.descendingKeySet()) {
            ArrayList<Integer> listaTerminosKveces = listaOrdenada.get(frecuencia);
            for (Integer archivo : listaTerminosKveces) {
                listaArchivos.add(new Integer[]{archivo, frecuencia});
            }
        }
        return listaArchivos;

    }



    public int getFrecuencia() { //devuelve la frecuencia total del término
        return frecuencia;
    }

    //  public void setFrecuencia(int frec) { //establece la frecuencia total del término
    //     frecuencia = frec;
    // }

    public int getFrecuenciaArchivo(Integer idArchivo) { //devuelve la frecuencia del término en el archivo
        Integer cont = archivos.get(idArchivo);
        if (cont == null) return 0;
        else return cont.intValue();
    }

    // public void setFrecuenciaArchivo(Integer idArchivo, int frec) { //establece la frecuencia del término en el archivo
    //     archivos.put(idArchivo, frec);
    // }   
    
}
