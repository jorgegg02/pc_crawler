import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.tika.*;


class Crawler {

    private String TOKEN_SEPARATOR = " ,.:;(){}!°?\t''%/|[]<=>&#+*$-¨^~";

    private TreeMap<String, Ocurrencia> terminos;
    private TreeMap<String, List<String>> thesauro;


    public Crawler() {
        terminos = new TreeMap<>();
        thesauro = new TreeMap<>();
    }

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

    private void llenarthesauro() {

        File file = new File("thesauro.txt");
    
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
    
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                List<String> sinonimos = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(line, TOKEN_SEPARATOR);
                //creamos una lista con los sinonimos
                while (st.hasMoreTokens()) {
                    String termino = st.nextToken();
                    termino = normalizeToken(termino);
                    sinonimos.add(termino);
                }
                
                //añadimos los sinonimos al thesauro
                for (String termino : new ArrayList<>(sinonimos)) {
                    if (!thesauro.containsKey(termino)) {
                        thesauro.put(termino, new ArrayList<>(sinonimos));
                    } else {
                        // Si ya existe el término, ampliamos los sinónimos con los nuevos
                        List<String> sinonimosAntiguos = thesauro.get(termino);
                        for (String nuevoSinonimo : sinonimos) {
                            if (!sinonimosAntiguos.contains(nuevoSinonimo)) {
                                sinonimosAntiguos.add(nuevoSinonimo);
                            }
                        }
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @SuppressWarnings("unchecked")
    private void cargarthesauro() {
        
        
        File file = new File("thesauro.ser");
        if (!file.exists()) {
            System.out.println("No existe el fichero theasaurus.ser, creando thesauro...");
            llenarthesauro();
            salvarObjeto("thesauro.ser", thesauro);
        }
        else {
            Object obj = leerObjeto("thesauro.ser");
            if (obj instanceof Map) {
                thesauro = (TreeMap<String, List<String>>) obj;
            }
        }
        
    }
    
    private void insertarEnDiccionario (String termino, File file) {
        // Obtenemos el término del thesauro
        List<String> sinonimos = thesauro.get(termino);
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
                    System.out.println("ERROR. No se puede leer el directorio " + file.getAbsolutePath());
                    continue;
                }
                File[] files = file.listFiles();
                if (files != null) {
                    cola.addAll(Arrays.asList(files));
                }
            } else {
            
                    processFile(file);
                
            }
        }
    }
    
    private void processFile(File file) {
        try {
            if (isSupportedFileType(file)) {
                llenarTerminosTextPlain(file);
            } else {
                llenarTerminosTika(file);
            }
            
        
        } 
        catch (FileNotFoundException e) {
            System.out.println("ERROR.FileNotFoundException No se puede leer el fichero " + file.getAbsolutePath());
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("ERROR.IOException No se puede leer el fichero " + file.getAbsolutePath());
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("ERROR.Exception No se puede leer el fichero " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    
    private boolean isSupportedFileType(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".txt") || fileName.endsWith(".java") || fileName.endsWith(".c") || fileName.endsWith(".cpp");
    }

    @SuppressWarnings("unchecked")
    private TreeMap<String, Ocurrencia> cargarDiccionarioTerminos(String puntoEntrada) {
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

    private void imprimirConsultaSinonimosTermino(String termino) {
        List<String> sinonimos = new ArrayList<>();
        sinonimos = thesauro.get(termino);
            //ordenamos sinonimos en base a la frecuencia
            sinonimos = sinonimos.stream().sorted((s1, s2) -> {
                Ocurrencia o1 = terminos.get(s1);
                Ocurrencia o2 = terminos.get(s2);
                int frecuencia1 = o1 == null ? 0 : o1.getFrecuencia();
                int frecuencia2 = o2 == null ? 0 : o2.getFrecuencia();
                if (o1 == null && o2 == null || frecuencia1 == frecuencia2) {
                    return 0;
                }
                if (o1 == null || frecuencia2 > frecuencia1) {
                    return 1;
                }
                if (o2 == null || frecuencia2 < frecuencia1) {
                    return -1;
                }
                return o2.getFrecuencia() - o1.getFrecuencia();
            }).collect(Collectors.toList());
            
            if (sinonimos != null) {
                
                for (String sinonimo : sinonimos) {
                    if (sinonimo.equals(termino)) {
                        continue;
                    }
                    Ocurrencia ocurrenciaSinonimo = terminos.get(sinonimo);
                    if (ocurrenciaSinonimo != null) {
                        
                        System.out.println("El término sinónimo \"" + sinonimo + "\" aparece " + ocurrenciaSinonimo.getFrecuencia() + " veces en los siguientes ficheros:");
                        System.out.println(ocurrenciaSinonimo.getArchivos());
                    }
                }
            }
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
                System.out.println("El término \"" + userInput + "\" aparece " + ocurrencia.getFrecuencia() + " veces en los siguientes ficheros:");
                System.out.println("Aparece en los siguientes ficheros:");
                System.out.println(ocurrencia.getArchivos());
            }
            imprimirConsultaSinonimosTermino(termino);

        }
        scanner.close();
    }

    public void run(String puntoEntrada) {
        
        // 1. Cargar thesauro
        cargarthesauro();

        if (this.thesauro == null) {
            System.out.println("ERROR. thesauro no cargado.");
            System.exit(0);
        }

        // 2. Cargar diccionario de términos
        cargarDiccionarioTerminos(puntoEntrada);

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