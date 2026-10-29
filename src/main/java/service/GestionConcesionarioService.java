package service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.Coche;
import model.Concesionario;
import model.Equipamiento;

import java.io.File;

public class GestionConcesionarioService {

    private static Concesionario concesionario;

    public GestionConcesionarioService() {
    }

    public static void insertarCocheNuevo(String marca, String matricula, String modelo, String extraEquipamiento){
        if (extraEquipamiento.equals("")){

        }


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
