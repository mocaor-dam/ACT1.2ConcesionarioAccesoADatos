package service;

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
import java.util.ArrayList;
import java.util.List;

public class GestionConcesionarioService {

    private static Concesionario concesionario;

    public GestionConcesionarioService() {
    }


    public static void inicializar() throws AppException, JAXBException {
        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        concesionario = unmarshalling(rutaXml);

        //Si el fichero XML estaba vacio lo creamos
        if (concesionario == null){
            concesionario = new Concesionario();
        }
    }

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

                        concesionario.getCoches().add(coche);
                    }
                }
            }

            marshalling(concesionario, rutaXml);


        } catch (IOException | JAXBException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertarCocheNuevo(Coche coche) throws MatriculaRepetidaException, JAXBException, AppException {
        if (concesionario.existeMatricula(coche.getMatricula())){
            throw new MatriculaRepetidaException("Error: Esa matricula ya existe.");
        }

        concesionario.getCoches().add(coche);
        String rutaXml = PropertiesReader.getInstance().get("ruta.xml");
        marshalling(concesionario, rutaXml);

    }

    private static void marshalling(Concesionario concesionario, String nombreFichero) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);


        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        File archivoXML = new File(nombreFichero);

        marshaller.marshal(concesionario, archivoXML);

    }

    private static Concesionario unmarshalling(String nombreFichero) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        File fichero = new File(nombreFichero);

        Concesionario concesionario = (Concesionario) unmarshaller.unmarshal(fichero);

        return concesionario;
    }

}
