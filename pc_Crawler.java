import java.io.*;
import java.util.*;
import java.text.Normalizer;
import org.apache.tika.*;


/* 

# Compilar
javac -cp .;lib/* pc_Crawler.java

# Ejecutar
java -cp .;lib/* pc_Crawler prueba

*/

public class pc_Crawler {

    private static String DELIMITER = "/::/";

    private static TreeMap<String, Ocurrencia> dic = new TreeMap<>();
    
    private static TreeMap<String, List<String>> thesaurus = new TreeMap<>();

    public static void crawling (String puntoEntrada, String separadores) {
        
        Queue<File> queue = new LinkedList<>();
        
        File fichero = new File(puntoEntrada);
    
        queue.add(new File(puntoEntrada));
    
        while (!queue.isEmpty()) {
            fichero = queue.poll();
            System.out.println(fichero.getName());
    
            if (fichero.isDirectory()) {
                if (!fichero.exists() || !fichero.canRead()) {
                    System.out.println("ERROR. No puedo leer " + fichero);
                    continue;
                }
                File[] listaFicheros = fichero.listFiles();
                if (listaFicheros != null) {
                    queue.addAll(Arrays.asList(listaFicheros));
                }
            }
            else try {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fichero), "UTF-8")
                );
                String linea;
                while ((linea=br.readLine()) != null) {
                    linea = linea.toLowerCase();
                    StringTokenizer st = new StringTokenizer(linea, separadores);
                    while (st.hasMoreTokens () ) {
                
                        String s = st.nextToken().toLowerCase();
                        s = Normalizer.normalize(s, Normalizer.Form.NFD);
                        s = s.replaceAll("[^\\p{ASCII}]", DELIMITER);
                        
                        System.out.println(s);

                        Object o = thesaurus.get(s);
                        System.out.println(o);

                        if (o != null){
                            o = dic.get(s);
                            if (o == null) dic.put(s, new Ocurrencia( fichero.getAbsolutePath()));
                            else {
                                
                                Ocurrencia ocurrencia = dic.get(s);
                                ocurrencia.addOcurrencia(fichero.getAbsolutePath());
                            }
                        }
                    }
                }
                br.close();
            }
            catch (Exception fnfe) {
                System.out.println("ERROR. Fichero desaparecido en combate  ;-)");
                System.out.println(fnfe);
            }
        } 

    }

    public static void  llenarThesauro () {
        
            File fichero = new File("theasaurus.txt");
    
            System.out.println(fichero.getName());
   
            try {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fichero), "UTF-8")
                );
                String linea;
                while ((linea=br.readLine()) != null) {

                    StringTokenizer st = new StringTokenizer(linea, ",.:;(){}!°?\t''%/|[]<=>&#+*$-¨^~");
                    
                    List<String> sinonimos = new ArrayList<String>();
                    while (st.hasMoreTokens () ) {
                       
                        String s = st.nextToken().toLowerCase();
                        // Replace all accented characters with a scapecified replacement
                        s = Normalizer.normalize(s, Normalizer.Form.NFD);
                        s = s.replaceAll("[^\\p{ASCII}]", DELIMITER);

                        sinonimos.add(s);
                    }

                    for (String s : sinonimos) {
                        thesaurus.put(s, sinonimos);
                    }
                    
                }
                br.close();
            }
            catch (Exception fnfe) {
                System.out.println("ERROR. Thesauro.txt desaparecido en combate  ;-)");
            }
    } 

    public static void crearThesauro() {
        // Comprobamos que existe un fichero llamado theasaurus.ser y si existe, cargamos el theasaurus usando readObject("theasaurus.ser", TreeMap(theasaurus)
        // Si no existe, creamos un theasaurus y lo guardamos en el fichero theasaurus.ser
        File f2 = new File("thesaurus.ser");
        if (!f2.exists()) {
            System.out.println("No existe el fichero theasaurus.ser");
            llenarThesauro();
            // for (Map.Entry<String, List<String>> entry : thesaurus.entrySet()) {
            //     System.out.println(entry.getKey() + " => " + entry.getValue());
            // }
            salvarObjeto("thesaurus.ser", thesaurus);
        }

        thesaurus = (TreeMap <String, List<String>>) leerObjeto("thesaurus.ser");
    }
    
    public static void salvarObjeto (String fichero, Object o) {
        try (FileOutputStream fos = new FileOutputStream(fichero);
            ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(o);
        }
        catch (Exception e) { System.out.println(e); }
    }

    @SuppressWarnings("unchecked")
    public static Object leerObjeto (String fichero) {
        Object o = null;
        try (FileInputStream fis = new FileInputStream(fichero);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
                o = ois.readObject();
                if (!(o instanceof Map)) {
                System.out.println("El objeto leído no es un Map<String, Object>");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return  o;
    }

    public static void crearDiccionario(String arg) {
        // Comprobamos si existe un diccionario y si existe, cargamos el diccionario usando readObject("diccionario.ser", TreeMap(dic)
        // Si no existe, creamos un diccionario usando el crawler y lo guardamos en el fichero diccionario.ser
        File f = new File("diccionario.ser");
        if ( !f.exists()) {
            System.out.println("No existe el fichero diccionario.ser");
            crawling(arg, " ,.:;(){}!°?\t''%/|[]<=>&#+*$-¨^~");
            salvarObjeto("diccionario.ser", dic);
        }
        
        dic = (TreeMap <String, Ocurrencia>) leerObjeto("diccionario.ser");
    }

    public static void consultas(){

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "ISO-8859-1"));
        
            while (true) {
                System.out.println("Introduce un término (o 'fin' para terminar):");
                String term = br.readLine().toLowerCase();
                
                if (term.equals("fin")) {
                    break;
                }

                System.out.println(term);
                term = Normalizer.normalize(term, Normalizer.Form.NFD);
                System.out.println(term);
                term = term.replaceAll("[^\\p{ASCII}]+", DELIMITER);
                System.out.println(term);

                Ocurrencia ocurrencia = dic.get(term);
                if (ocurrencia == null) {
                    System.out.println("El término no se encuentra en el diccionario.");
                } else {
                    System.out.println("El término aparece " + ocurrencia.getFrecuencia() + " veces en total.");
                    System.out.println("Aparece en los siguientes ficheros:");
                    System.out.println(ocurrencia.getArchivos());
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
 
    // --------------------MAIN--------------------
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("ERROR. Utilizar: >java ejercicio fichero_entrada");
            return;
        }

        crearThesauro();
        
        crearDiccionario(args[0]);
        
    
        // System.out.println(dic);
        // System.out.println(thesaurus);

        // Consultas
        consultas();
        
    }
}