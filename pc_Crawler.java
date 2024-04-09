import java.io.*;
import java.util.*;

import org.apache.tika.*;


class Crawler {

    private String TOKEN_SEPARATOR = " ,.:;(){}!°?\t''%/|[]<=>&#+*$-¨^~";

    private TreeMap<String, Ocurrencia> terminos = new TreeMap<>();
    private TreeMap<String, List<String>> thesaurus = new TreeMap<>();

    private void salvarObjeto(String nombreFichero, Object objeto) {
        
        try {
            FileOutputStream fos = new FileOutputStream(nombreFichero);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(objeto);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object leerObjeto(String fichero) {
        
        Object obj = null;
        try (FileInputStream fis = new FileInputStream(fichero);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
                obj = ois.readObject();
                if (!(obj instanceof Map)) {
                System.out.println("El objeto leído no es un Map<String, Object>");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return obj;
    }

    private String normalizeToken(String token) {
        token = token.toLowerCase();
        token = token.replaceAll("[á]", "a")
            .replaceAll("[é]", "e")
            .replaceAll("[í]", "i")
            .replaceAll("[ó]", "o")
            .replaceAll("[ú]", "u");
        token = token.replaceAll("[^\\p{ASCII}]", "");
        return token;
    }

    private TreeMap<String, List<String>> llenarThesaurus() {
        
        File file = new File("thesaurus.txt");
        TreeMap<String, List<String>> thesaurus = new TreeMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                List<String> sinonimos = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(line, TOKEN_SEPARATOR);
                while (st.hasMoreTokens()) {
                    String termino = st.nextToken();
                    termino = normalizeToken(termino);
                    sinonimos.add(termino);
                }
                for (String termino : sinonimos) {
                    thesaurus.put(termino, sinonimos);
                }

               
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return thesaurus;
    }

    @SuppressWarnings("unchecked")
    private TreeMap<String, List<String>> cargarThesaurus() {
        
        TreeMap<String, List<String>> thesaurus = null;
        File file = new File("thesaurus.ser");
        if (!file.exists()) {
            System.out.println("No existe el fichero theasaurus.ser, creando thesaurus...");
            thesaurus = llenarThesaurus();
            salvarObjeto("thesaurus.ser", thesaurus);
        }
        Object obj = leerObjeto("thesaurus.ser");
        if (obj instanceof TreeMap) {
            thesaurus = (TreeMap<String, List<String>>) obj;
        }
        return thesaurus;
    }
    
    private void insertarEnDiccionario (String termino, File file) {
        // Obtenemos el término del thesaurus
        List<String> sinonimos = thesaurus.get(termino);
        if (sinonimos != null) {
            // Si existen sinónimos o el propio término, obtenemos la ocurrencia
            Ocurrencia ocurrencia = terminos.get(termino);
            if (ocurrencia == null) {
                // Si no existe la ocurrencia, la creamos
                terminos.put(termino, new Ocurrencia(file.getAbsolutePath()));
            } else {
                // Si existe la ocurrencia, la incrementamos
                ocurrencia.addOcurrencia(file.getAbsolutePath());
            }
        }
    }

    private void llenarTerminosTextPlain(File file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.toLowerCase();
            StringTokenizer st = new StringTokenizer(line, TOKEN_SEPARATOR);
            while (st.hasMoreTokens()) {
                String termino = st.nextToken();
                termino = normalizeToken(termino);
                insertarEnDiccionario(termino, file);
            }
        }
        br.close();
    }


    private void llenarTerminosTika(File file) throws Exception {
        Tika tika = new Tika();
        String contenido;
        try {
            contenido = tika.parseToString(file);
            StringTokenizer st = new StringTokenizer(contenido, TOKEN_SEPARATOR);
            while (st.hasMoreTokens()) {
                String termino = st.nextToken();
                termino = normalizeToken(termino);
                insertarEnDiccionario(termino, file);
            }
        } catch (Exception e) {
            throw new Exception("ERROR. Tika no puede leer " + file);
        }
    }

    private void llenarTerminos(String puntoEntrada) {
        Queue<File> cola = new LinkedList<>();
        File file = new File(puntoEntrada);
        cola.add(file);
        while(!cola.isEmpty()) {
            file = cola.poll();
            if (file.isDirectory()) {
                if (!file.exists() || !file.canRead()) {
                    System.out.println("ERROR. No se puede leer el directorio " + file.getName());
                    continue;
                }
                File[] files = file.listFiles();
                if (files != null) {
                    cola.addAll(Arrays.asList(files));
                }
            } else try {
                if (
                    file.getName().endsWith(".txt") ||
                    file.getName().endsWith(".java") ||
                    file.getName().endsWith(".c") ||
                    file.getName().endsWith(".cpp")
                ) {
                    llenarTerminosTextPlain(file);
                } else {
                    llenarTerminosTika(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private TreeMap<String, Ocurrencia> cargarTerminos(String puntoEntrada) {
        File file = new File("diccionario.ser");
        if (!file.exists()) {
            System.out.println("No existe el fichero diccionario.ser, creando diccionario...");
            llenarTerminos(puntoEntrada);
            salvarObjeto("diccionario.ser", terminos);
        }
        Object obj = leerObjeto("diccionario.ser");
        if (obj instanceof TreeMap) {
            terminos = (TreeMap<String, Ocurrencia>) obj;
        }
        return terminos;
    }

    private void consultas() {
        Scanner scanner = new Scanner(System.in);
        String end_word = "fin";
        while(true) {
            System.out.print("Introduce un término (o " + end_word + " para terminar): ");
            String userInput = scanner.nextLine();
            if (userInput.equals(end_word)) {
                break;
            }
            String termino = normalizeToken(userInput);
            Ocurrencia ocurrencia = terminos.get(termino);
            if (ocurrencia == null) {
                System.out.println("El término " + userInput + " no existe en el diccionario.");
            } else {
                System.out.println("El término " + userInput + " aparece " + ocurrencia.getFrecuencia() + " veces en los siguientes ficheros:");
                System.out.println("Aparece en los siguientes ficheros:");
                System.out.println(ocurrencia.getArchivos());
            }
        }
        scanner.close();
    }

    public void run(String puntoEntrada) {
        
        // 1. Cargar thesaurus
        this.thesaurus = cargarThesaurus();

        if (this.thesaurus == null) {
            System.out.println("ERROR. Thesaurus no cargado.");
            System.exit(0);
        }

        // 2. Cargar diccionario de términos
        cargarTerminos(puntoEntrada);

        if (this.terminos == null) {
            System.out.println("ERROR. Diccionario de términos no cargado.");
            System.exit(0);
        }

        // 3. Hacer consultas
        consultas();
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Uso: java Crawler <puntoEntrada>");
            System.exit(0);
        }

        new Crawler().run(args[0]);
    }

}