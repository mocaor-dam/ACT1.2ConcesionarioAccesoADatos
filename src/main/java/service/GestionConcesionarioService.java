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
     *
     * @throws AppException
     * @throws JAXBException
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
     *
     * @throws AppException
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
     * Comprobamos si existe la matricula
     * @param coche
     * @throws MatriculaRepetidaException
     * @throws JAXBException
     * @throws AppException
     */
    public static void insertarCocheNuevo(Coche coche) throws MatriculaRepetidaException, JAXBException, AppException {
        if (concesionario.existeMatricula(coche.getMatricula())){
            throw new MatriculaRepetidaException("Error: Esa matricula ya existe.");
        }
        //Asignamos el id del coche
        coche.setId(concesionario.getCoches().size() + 1);

        concesionario.getCoches().add(coche);
        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        marshalling(concesionario, rutaXml);

    }

    /**
     *
     * @throws JAXBException
     * @throws AppException
     */
    public static void ordenarPorMatricula() throws JAXBException, AppException {
        concesionario.getCoches().sort(Comparator.comparing(Coche::getMatricula));

        reorganizarIds();

        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        marshalling(concesionario, rutaXml);
    }


    /**
     *
     * @param matricula
     * @throws AppException
     * @throws JAXBException
     */
    public static void borrarRegistroPorMatricula(String matricula) throws AppException, JAXBException {
        if (concesionario.existeMatricula(matricula)){
            concesionario.getCoches().removeIf(coche -> coche.getMatricula().equalsIgnoreCase(matricula));

            reorganizarIds();

            String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
            marshalling(concesionario, rutaXml);
        } else {
            throw new AppException("No se encontro ningun coche con la matricula: " + matricula);
        }
    }

    /**
     *
     * @param matricula
     * @param marca
     * @param modelo
     * @param equipamientos
     * @throws JAXBException
     * @throws AppException
     */
    public static void modificarRegistro(String matricula, String marca, String modelo, List<Equipamiento> equipamientos) throws JAXBException, AppException {
        if (concesionario.existeMatricula(matricula)){
           Coche cocheAModificar = concesionario.getCoches().stream()
                   .filter(coche -> coche.getMatricula().equalsIgnoreCase(matricula))
                   .findFirst().orElseThrow(() -> new AppException("Error: No existe ningun coche con esa matricula"));

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
     *
     * @throws AppException
     */
    public static void exportarConcesionarioJson() throws AppException {
        String rutaJson = PropertiesReader.getInstance().get("ruta.json");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        try (Writer writer = Files.newBufferedWriter(Path.of(rutaJson), StandardCharsets.UTF_8)){
            gson.toJson(concesionario, writer);
        } catch (IOException e) {
            throw new AppException("Error al exportar la base de datos a JSON: " + e.getMessage());
        }
    }

    /**
     *
     * @param matricula
     * @param rutaFicheroSalida
     * @throws AppException
     */
    public static void exportarCocheJson(String matricula, String rutaFicheroSalida) throws AppException {
        if (!concesionario.existeMatricula(matricula)){
            throw new AppException("No existe ningun coche con la matricula " + matricula);
        }

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
     *
     * @param rutaJson
     * @throws AppException
     */
    public static void importarConcesionarioJson(String rutaJson) throws AppException {
        Gson gson = new Gson();

        try (Reader reader = Files.newBufferedReader(Path.of(rutaJson), StandardCharsets.UTF_8)){
            Concesionario concesionarioNuevo = gson.fromJson(reader, Concesionario.class);
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
     *
     * @param rutaJson
     * @throws AppException
     */
    public static void importarCocheJson(String rutaJson) throws AppException {
        Gson gson = new Gson();

        try (Reader reader = Files.newBufferedReader(Path.of(rutaJson), StandardCharsets.UTF_8)){
            Coche cocheImportado = gson.fromJson(reader, Coche.class);
            if (cocheImportado != null && cocheImportado.getMatricula() != null){

                if (concesionario.existeMatricula(cocheImportado.getMatricula())){
                    throw new AppException("El coche con la matricula " + cocheImportado.getMatricula() + " ya existe en la base de datos");
                }

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
     *
     * @throws AppException
     */
    public static void generarInformeResumen() throws AppException {
       String rutaInforme = PropertiesReader.getInstance().get("ruta.informe");


        //Numero de coches en el concesionario
        int numCoches = concesionario.getCoches().size();

        //Coches agrupados por marca
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
                        int cantidadActual = recuentosExtras.get(nombreExtra);
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
        for (Map.Entry<String, Integer> entry : recuentosExtras.entrySet()){
            if (entry.getValue() > maxRepeticiones){
                maxRepeticiones = entry.getValue();
                extraMasRepetido = entry.getKey();
            }
        }


        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(rutaInforme), StandardCharsets.UTF_8)){
           bufferedWriter.write("INFORME RESUMEN DEL CONCESIONARIO");
           bufferedWriter.newLine();
           bufferedWriter.write("Numero total de coches: " + numCoches);
           bufferedWriter.newLine();
           bufferedWriter.newLine();
           bufferedWriter.write("Lista de coches agrupados por marca");
           bufferedWriter.newLine();
           for (Map.Entry<String, List<Coche>> entry : cochesPorMarca.entrySet()){
               bufferedWriter.write("Marca: " + entry.getKey() + entry.getValue().size() + " coches");
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
     *
     * @param concesionario
     * @param nombreFichero
     * @throws JAXBException
     */
    private static void marshalling(Concesionario concesionario, String nombreFichero) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);


        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        File archivoXML = new File(nombreFichero);

        marshaller.marshal(concesionario, archivoXML);

    }

    /**
     *
     * @param nombreFichero
     * @return
     * @throws JAXBException
     */
    private static Concesionario unmarshalling(String nombreFichero) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        File fichero = new File(nombreFichero);

        Concesionario concesionario = (Concesionario) unmarshaller.unmarshal(fichero);

        return concesionario;
    }


    private static void reorganizarIds(){
        List<Coche> coches = concesionario.getCoches();

        for (int i = 0; i < coches.size(); i++) {
            coches.get(i).setId(i + 1);
        }
    }

}
