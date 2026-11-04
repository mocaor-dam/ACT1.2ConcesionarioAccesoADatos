package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.AppException;
import exceptions.MatriculaRepetidaException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.Coche;
import model.Concesionario;
import model.Equipamiento;
import utils.PropertiesReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GestionConcesionarioService {

    private static Concesionario concesionario;

    public GestionConcesionarioService() {
    }

    /**
     * En este metodo se realiza el unmarshalling, es el metodo necesario para iniciar el programa
     * ya que es el metodo que carga los datos dentro del objeto de tipo Concesionario
     * No devuelve nada
     * @throws AppException Si no encuentra la ruta.xml
     * @throws JAXBException Si falla el unmarshalling
     */
    public static void inicializar() throws AppException, JAXBException {
        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        concesionario = unmarshalling(rutaXml);

        //Si el fichero XML estaba vacio lo creamos
        if (concesionario == null){
            concesionario = new Concesionario();
        }
    }

    /**
     * Metodo encargado de la importacion del csv, no devuelve nada
     * @throws AppException Si falla al importar el csv
     */
    public static void importarDesdeCsv() throws AppException {
        String rutaCsv = PropertiesReader.getInstance().get("ruta.csv");
        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");

        try (BufferedReader br = Files.newBufferedReader(Path.of(rutaCsv), StandardCharsets.UTF_8)){

            String cabecera = br.readLine();

            String linea;
            while ((linea = br.readLine()) != null){
                if (!linea.isBlank()){
                    String[] campos = linea.split(";");

                    String matricula = campos[0];


                    if (!concesionario.existeMatricula(campos[0])){
                        String marca = campos[1];
                        String modelo = campos[2];

                        List<Equipamiento> extras = new ArrayList<>();

                        if (campos.length > 3 && !campos[3].isBlank()){
                            String[] arrayExtras = campos[3].split("\\|");
                            for (String nombreExtra : arrayExtras){
                                Equipamiento e = new Equipamiento();
                                e.setNombre(nombreExtra);
                                extras.add(e);
                            }
                        }

                        Coche coche = new Coche();
                        coche.setMatricula(matricula);
                        coche.setMarca(marca);
                        coche.setModelo(modelo);
                        coche.setEquipamientos(extras);

                        coche.setId(concesionario.getCoches().size() + 1);


                        concesionario.getCoches().add(coche);
                    }
                }
            }

            marshalling(concesionario, rutaXml);


        } catch (IOException | JAXBException e) {
            throw new AppException("Error al procesar la importacion del CSV" + e.getMessage());
        }
    }

    /**
     * Metodo encargado de la insercion de coches, no devuelve nada
     * @param coche coche creado con los datos pedidos en la clase principal
     * @throws MatriculaRepetidaException Si ya existe esa matricula
     * @throws JAXBException Si falla el marshalling
     * @throws AppException Si no encuentra la ruta.xml
     */
    public static void insertarCocheNuevo(Coche coche) throws MatriculaRepetidaException, JAXBException, AppException {
        //Comprobamos si existe la matricula con el metodo creado en la clase Concesionario
        if (concesionario.existeMatricula(coche.getMatricula())){
            throw new MatriculaRepetidaException("Error: Esa matricula ya existe.");
        }
        //Asignamos el id del coche
        coche.setId(concesionario.getCoches().size() + 1);
        //Añadimos el coche a la lista de coches
        concesionario.getCoches().add(coche);

        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        marshalling(concesionario, rutaXml);

    }

    /**
     * Metodo encargado de la ordenacion por matricula, no devuelve nada
     * @throws JAXBException Si falla el marshalling
     * @throws AppException Si no encuentra la ruta.xml
     */
    public static void ordenarPorMatricula() throws JAXBException, AppException {
        //Usamos un comparator para ordenar alfabeticamente por matricula
        concesionario.getCoches().sort(Comparator.comparing(Coche::getMatricula));

        //Llamo al metodo auxiliar que se encarga de reorganizar el id de los coches
        reorganizarIds();

        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        marshalling(concesionario, rutaXml);
    }


    /**
     * Metodo encargado del borrado de coches por matricula, no devuelve nada
     * @param matricula matricula por la que se va a buscar el coche para eliminarlo
     * @throws AppException Si no se encuentra coche con esa matricula
     * @throws JAXBException Si falla el marshalling
     */
    public static void borrarRegistroPorMatricula(String matricula) throws AppException, JAXBException {
        if (concesionario.existeMatricula(matricula)){
            //Borramos el coche SI coincide la matricula con la matricula introducida
            concesionario.getCoches().removeIf(coche -> coche.getMatricula().equalsIgnoreCase(matricula));

            reorganizarIds();

            String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
            marshalling(concesionario, rutaXml);
        } else {
            throw new AppException("No se encontro ningun coche con la matricula: " + matricula);
        }
    }

    /**
     * Metodo encargado de la modificacion de los registros
     * @param matricula matricula introducida por la que buscar el coche a modificar
     * @param marca la nueva marca que se le va a modificar por la anterior
     * @param modelo el nuevo modelo que se le va a modificar por el modelo anterior
     * @param equipamientos los nuevos equipamientos que se le van a modificar por el anterior
     * @throws JAXBException Si falla el marshalling
     * @throws AppException Si no encuentra un coche con esa matricula
     */
    public static void modificarRegistro(String matricula, String marca, String modelo, List<Equipamiento> equipamientos) throws JAXBException, AppException {
        if (concesionario.existeMatricula(matricula)){
           //Abrimos un flujo donde filtramos por matricula, buscando el coche que coincida con la matricula
            Coche cocheAModificar = concesionario.getCoches().stream()
                   .filter(coche -> coche.getMatricula().equalsIgnoreCase(matricula))
                   .findFirst().orElseThrow(() -> new AppException("Error: No existe ningun coche con esa matricula"));
           //Cambiamos los datos de ese coche por el nuevo con los setters
           cocheAModificar.setMarca(marca);
           cocheAModificar.setModelo(modelo);
           cocheAModificar.setEquipamientos(equipamientos);

           String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
           marshalling(concesionario,rutaXml);
        } else {
            throw new AppException("No se ha encontrado un coche con esa matricula");
        }
    }


    /**
     * Metodo encargado de la exportacion del concesionario completo a Json
     * @throws AppException Si da error al exportar la base de datos
     */
    public static void exportarConcesionarioJson() throws AppException {
        String rutaJson = PropertiesReader.getInstance().get("ruta.json");

        //He usado la libreria Gson, ya que me resulta mas sencilla
        //Creamos un Gson con el GsonBuilder, y le ponemos setPrettyPrinting para que tenga un formato humano y mas legible
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Abrimos el canal de escritura donde le pasamos la ruta del properties rutaJson, con el formato UTF_8 para que no de error las letras españolas,
        //como la Ñ o las tildes
        try (Writer writer = Files.newBufferedWriter(Path.of(rutaJson), StandardCharsets.UTF_8)){
            //Usamos el metodo toJson de la libreria Gson para pasarlo al fichero JSON
            gson.toJson(concesionario, writer);
        } catch (IOException e) {
            throw new AppException("Error al exportar la base de datos a JSON: " + e.getMessage());
        }
    }

    /**
     * Metodo encargado de la exportacion de UN solo coche a JSON
     * @param matricula es el parametro por el que se busca el coche
     * @param rutaFicheroSalida es la ruta del fichero Json al que se va a exportar
     * @throws AppException Si da error al exportar el coche
     */
    public static void exportarCocheJson(String matricula, String rutaFicheroSalida) throws AppException {
        if (!concesionario.existeMatricula(matricula)){
            throw new AppException("No existe ningun coche con la matricula " + matricula);
        }

        //Abrimos un flujo donde filtramos por matricula, buscando el coche que coincida con la matricula
        Coche cocheEncontrado = concesionario.getCoches().stream()
                .filter(coche1 -> coche1.getMatricula().equalsIgnoreCase(matricula))
                .findFirst().orElseThrow(() -> new AppException("Error al buscar el coche con la siguiente matricula: " + matricula));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = Files.newBufferedWriter(Path.of(rutaFicheroSalida), StandardCharsets.UTF_8)){
            gson.toJson(cocheEncontrado, writer);
        } catch (IOException e){
            throw new AppException("Error al exportar el coche " + e.getMessage());
        }
    }

    /**
     * Metodo encargado de la importacion del concesionario desde un fichero JSON
     * @param rutaJson la ruta desde donde se va a importar los datos del concesionario
     * @throws AppException Si da error al importar el concesionario o si el concesionario o su lista de coches esta vacio
     */
    public static void importarConcesionarioJson(String rutaJson) throws AppException {
        Gson gson = new Gson();

        //Abrimos un canal de lectura hacia la ruta que le pasamos por parametros
        try (Reader reader = Files.newBufferedReader(Path.of(rutaJson), StandardCharsets.UTF_8)){
            //Creamos un nuevo concesionario con los datos que se obtengan desde el JSON con el otro metodo de Gson llamado fromJson
            Concesionario concesionarioNuevo = gson.fromJson(reader, Concesionario.class);

            //Comprobamos que no sea nulo y que la lista de coches del concesionario no este vacia
            if (concesionarioNuevo != null && concesionarioNuevo.getCoches() != null) {
                //Reemplazamos la base de datos
                concesionario = concesionarioNuevo;

                reorganizarIds();

                String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
                marshalling(concesionario, rutaXml);
            } else {
                throw new AppException("El archivo no contiene un concesionario valido");
            }
        } catch (IOException | JAXBException e) {
            throw new AppException("Error al importar el concesionario");
        }
    }

    /**
     * Metodo encargado de la importacion de UN solo coche desde un fichero Json
     * @param rutaJson es la ruta del fichero Json del que vamos a importar un coche
     * @throws AppException Si ocurre un error al leer el archivo o ya existe esa matricula
     */
    public static void importarCocheJson(String rutaJson) throws AppException {
        Gson gson = new Gson();

        try (Reader reader = Files.newBufferedReader(Path.of(rutaJson), StandardCharsets.UTF_8)){
            //Usamos el mismo metodo fromJson para obtener los datos del fichero, pero esta vez para sacar un objeto de tipo Coche
            Coche cocheImportado = gson.fromJson(reader, Coche.class);
            if (cocheImportado != null && cocheImportado.getMatricula() != null){
                //Comprobamos que la matricula no exista antes de añadirlo a la lista de coches de Concesionario
                if (concesionario.existeMatricula(cocheImportado.getMatricula())){
                    throw new AppException("El coche con la matricula " + cocheImportado.getMatricula() + " ya existe en la base de datos");
                }

                //Le asignamos un id al coche
                cocheImportado.setId(concesionario.getCoches().size() + 1);

                //Añadimos el coche a la lista de concesionario
                concesionario.getCoches().add(cocheImportado);
                String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
                marshalling(concesionario, rutaXml);

            }
        } catch (AppException | JAXBException | IOException e) {
            throw new AppException("Error al importar el coche desde JSON " + e.getMessage());
        }
    }

    /**
     * Metodo encargado de generar un informe resumen
     * @throws AppException
     */
    public static void generarInformeResumen() throws AppException {
       String rutaInforme = PropertiesReader.getInstance().get("ruta.informe");


        //Numero de coches en el concesionario
        int numCoches = concesionario.getCoches().size();

        //Obtenemos los coches agrupados por marca con un flujo de la lista de coches de concesionario,
        // donde con el metodo collect, agrupamos los coches con su marca
        Map<String, List<Coche>> cochesPorMarca = concesionario.getCoches().stream()
                .collect(Collectors.groupingBy(Coche::getMarca));

        //Equipamiento que mas se repite entre todos los coches
        Map<String, Integer> recuentosExtras = new HashMap<>();
        for (Coche c : concesionario.getCoches()){
            if (c.getEquipamientos() != null){
                for (Equipamiento eq : c.getEquipamientos()){
                    String nombreExtra = eq.getNombre();
                    //Si el mapa ya contiene ese extra, le sumamos 1
                    if (recuentosExtras.containsKey(nombreExtra)){
                        //Va al mapa y le pregunta cuantas veces habia salido antes
                        int cantidadActual = recuentosExtras.get(nombreExtra);
                        //Sobreescribe con .put() el valor antiguo sumandole 1
                        recuentosExtras.put(nombreExtra, cantidadActual + 1);
                    } else {
                        //Si es la primera vez que aparece, lo guardamos con valor 1
                        recuentosExtras.put(nombreExtra, 1);
                    }
                }
            }
        }

        //Buscamos el extra mas repetido
        String extraMasRepetido = "Ninguno";
        int maxRepeticiones = 0;
        //Usamos entrySet() para poder recorrer el mapa ya que nos devuelve una coleccion de las parejas clave-valor que contiene el mapa
        for (Map.Entry<String, Integer> entry : recuentosExtras.entrySet()){
            //Si las repeticiones del extra actual superan las del extra mas repetido, actualizamos el maximo
            if (entry.getValue() > maxRepeticiones){
                maxRepeticiones = entry.getValue();
                extraMasRepetido = entry.getKey();
            }
        }

        //Con Files.newBufferedWriter creamos o sobreescribimos un archivo de texto en la ruta guardada
        //Usamos un bufferedWriter para guardar el texto en un bufer intermedio de memoria para volcarlo to do de golpe
        // por que es la forma mas eficiente de escribir archivos en java
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(rutaInforme), StandardCharsets.UTF_8)){
           //Con bufferedWriter.write() escribimos
            bufferedWriter.write("INFORME RESUMEN DEL CONCESIONARIO");
            //Con bufferedWriter.newLine() pasamos a una nueva linea
           bufferedWriter.newLine();
           bufferedWriter.write("Numero total de coches: " + numCoches);
           bufferedWriter.newLine();
           bufferedWriter.newLine();
           bufferedWriter.write("Lista de coches agrupados por marca");
           bufferedWriter.newLine();
           for (Map.Entry<String, List<Coche>> entry : cochesPorMarca.entrySet()){
               bufferedWriter.write("Marca: " + entry.getKey() + "(" + entry.getValue().size() + " coches)");
               bufferedWriter.newLine();
               for (Coche coche : entry.getValue()){
                   bufferedWriter.write(" | Matricula: " + coche.getMatricula() + " | Modelo: " + coche.getModelo());
                   bufferedWriter.newLine();
               }
           }
           bufferedWriter.newLine();

           bufferedWriter.write("EXTRA MAS REPETIDO");
           bufferedWriter.newLine();
           if (maxRepeticiones > 0){
               bufferedWriter.write("El extra mas repetido es " + extraMasRepetido + " apareciendo " + maxRepeticiones + " veces");
           } else {
               bufferedWriter.write("No hay equipamientos registrados.");
           }

        } catch (IOException e) {
            throw new AppException("Error al hacer el informe de resumen " + e.getMessage());
        }

    }

    /**
     * Metodo encargado de la conversion de un objeto Java a un fichero XML
     * No devuelve nada
     * @param concesionario el objeto a pasar a XML
     * @param nombreFichero nombre del fichero XML al que se va a convertir
     * @throws JAXBException
     */
    private static void marshalling(Concesionario concesionario, String nombreFichero) throws JAXBException {
        //Creamos el contexto
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);

        //Creamos el marshaller
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        //Creamos un archivo con el nombre del fichero
        File archivoXML = new File(nombreFichero);

        //Llamamos al metodo marshal que pide un concesionario y un archivoXML
        marshaller.marshal(concesionario, archivoXML);

    }

    /**
     * Metodo encargado de la conversion de un fichero XML a un objeto Java
     * @param nombreFichero nombre del fichero del que se va a extraer la informacion
     * @return Concesionario el objeto en el que se va a volcar la informacion del fichero XML
     * @throws JAXBException
     */
    private static Concesionario unmarshalling(String nombreFichero) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);

        //Creamos el unmarshaller
        Unmarshaller unmarshaller = context.createUnmarshaller();

        File fichero = new File(nombreFichero);

        //Creamos un nuevo concesionario en el que le pasamos los datos del fichero Xml mediante un casting
        Concesionario concesionario = (Concesionario) unmarshaller.unmarshal(fichero);

        return concesionario;
    }

    /**
     * Metodo privado que se encarga de la organizacion de los IDs
     */
    private static void reorganizarIds(){
        List<Coche> coches = concesionario.getCoches();

        for (int i = 0; i < coches.size(); i++) {
            coches.get(i).setId(i + 1);
        }
    }

}
