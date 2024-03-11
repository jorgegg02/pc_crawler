import java.io.*;
import java.util.*;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class pc_Crawler {

    private static TreeMap<String, Integer> dic = new TreeMap<>();
    
    private static TreeMap<String, List<String>> thesaurus = new TreeMap<>();

    public static TreeMap crawling (String puntoEntrada, String separadores) {
        
        //TODO revisar parametrización de crawling
    
        Queue<File> queue = new LinkedList<>();
        Map <String, Integer> map = new TreeMap <String, Integer> ();
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
                FileReader fr = new FileReader(fichero);
                BufferedReader br = new BufferedReader(fr);
                String linea;
                while ((linea=br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(linea, separadores);
                    while (st.hasMoreTokens () ) {
                       
                        String s = st.nextToken().toLowerCase();

                        // s = s.replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i").replaceAll("ó", "o").replaceAll("ú", "u");
                        s = Normalizer.normalize(s, Normalizer.Form.NFD);
                        System.out.println(s);
                        Object o = thesaurus.get(s);
                        System.out.println(o);

                        if (o != null){
                            o = map.get(s);
                            if (o == null) map.put (s, new Integer (1));
                            else {
                                Integer cont = (Integer) o;
                                map.put (s, new Integer (cont.intValue () + 1));
                            }
                        }
                    }
                }
                br.close();
            }
            catch (Exception fnfe) {
                System.out.println("ERROR. Fichero desaparecido en combate  ;-)");
            }
        } 
        // for (Map.Entry<String, Integer> entry : map.entrySet()) {
        //     System.out.println(entry.getKey() + " : " + entry.getValue());
        // } 

        return (TreeMap) map;
    }

    public static TreeMap<String,  List<String>> crearThesauro (String puntoEntrada, String separadores) {
        
        //TODO revisar parametrización de crawling
    
        Queue<File> queue = new LinkedList<>();
        TreeMap <String, List<String>> map = new TreeMap <String,  List<String>> ();
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
                FileReader fr = new FileReader(fichero);
                BufferedReader br = new BufferedReader(fr);
                String linea;
                while ((linea=br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(linea, separadores);
                    
                    List<String> sinonimos = new ArrayList<String>();
                    while (st.hasMoreTokens () ) {
                       
                        String s = st.nextToken().toLowerCase();
                        // s = s.replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i").replaceAll("ó", "o").replaceAll("ú", "u");
                        s = Normalizer.normalize(s, Normalizer.Form.NFD);

                        sinonimos.add(s);
                    }

                    for (String s : sinonimos) {
                        map.put(s, sinonimos);
                    }
                    
                }
                br.close();
            }
            catch (Exception fnfe) {
                System.out.println("ERROR. Fichero desaparecido en combate  ;-)");
            }
        } 
        // for (Map.Entry<String, Integer> entry : map.entrySet()) {
        //     System.out.println(entry.getKey() + " : " + entry.getValue());
        // } 

        // System.out.println(map);
        return  map;
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
 
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("ERROR. Utilizar: >java ejercicio fichero_entrada");
            return;
        }

    
        
        // Comprobamos que existe un fichero llamado theasaurus.ser y si existe, cargamos el theasaurus usando readObject("theasaurus.ser", TreeMap(theasaurus)
        // Si no existe, creamos un theasaurus y lo guardamos en el fichero theasaurus.ser
        File f2 = new File("thesaurus.ser");
        if (!f2.exists()) {
            System.out.println("No existe el fichero theasaurus.ser");
            thesaurus = crearThesauro("theasaurus.txt", ",.:;(){}!°?\t''%/|[]<=>&#+*$-¨^~");
            salvarObjeto("thesaurus.ser", thesaurus);
        }

        thesaurus = (TreeMap <String, List<String>>) leerObjeto("thesaurus.ser");

        // Comprobamos si existe un diccionario y si existe, cargamos el diccionario usando readObject("diccionario.ser", TreeMap(dic)
        // Si no existe, creamos un diccionario usando el crawler y lo guardamos en el fichero diccionario.ser
        File f = new File("diccionario.ser");
        if ( !f.exists()) {
            System.out.println("No existe el fichero diccionario.ser");
            dic = crawling(args[0], " ,.:;(){}!°?\t''%/|[]<=>&#+*$-¨^~");
            salvarObjeto("diccionario.ser", dic);
        }
        
        dic = (TreeMap <String, Integer>) leerObjeto("diccionario.ser");
    
        // System.out.println(dic);
        // System.out.println(thesaurus);

        // Consultas
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Introduce un término (o 'fin' para terminar):");
            String term = scanner.nextLine();
            if (term.equals("fin")) {
                break;
            }
            Integer count = dic.get(term);
            if (count == null) {
                System.out.println("El término no se encuentra en el diccionario.");
            } else {
                System.out.println("El término aparece " + count + " veces.");
            }
        }
        scanner.close();
    }
}