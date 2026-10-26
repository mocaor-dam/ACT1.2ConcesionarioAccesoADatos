package model;

import jakarta.xml.bind.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CheckedOutputStream;

public class Principal {
    static void main(String[] args) throws JAXBException {
        //Unmarshalling
        JAXBContext context = JAXBContext.newInstance(Concesionario.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        File archivoXML = new File("concesionario_plantilla.xml");

        Concesionario concesionarioUnmarshalling = (Concesionario) unmarshaller.unmarshal(archivoXML);
        System.out.println("=== Unmarshalling ===");
        System.out.println("Concesionario Cargado: " + concesionarioUnmarshalling);



        //Marshalling
        Concesionario concesionarioMarshalling = new Concesionario();

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


        File nuevoArchivoXML = new File("concesionarioMio.xml");
        marshaller.marshal(concesionarioMarshalling, nuevoArchivoXML);


        /*
        List<Coche> coches = new ArrayList<>();

        //Creacion de coche 1
        Coche coche1 = new Coche();

        List<String> extras1 = new ArrayList<>();

        extras1.add("Climatizador");
        extras1.add("Navegador GPS");
        extras1.add("Techo solar");

        List<Equipamiento> equipamientosCoche1 = new ArrayList<>();
        Equipamiento equipamiento1 = new Equipamiento();

        equipamiento1.setExtras(extras1);
        equipamientosCoche1.add(equipamiento1);

        coche1.setId(1);
        coche1.setMatricula("1234ABC");
        coche1.setMarca("Renault");
        coche1.setModelo("Clio");
        coche1.setEquipamientos(equipamientosCoche1);

        coches.add(coche1);

        //Creacion coche 2
        Coche coche2 = new Coche();

        List<String> extras2 = new ArrayList<>();

        extras2.add("Llantas de aleación");
        extras2.add("Sensor de aparcamiento");

        Equipamiento equipamiento2 = new Equipamiento();

        equipamiento2.setExtras(extras2);

        coche2.setId(2);
        coche2.setMatricula("5678DEF");
        coche2.setMarca("Ford");
        coche2.setModelo("Focus");
        coche2.setEquipamiento(equipamiento2);

        coches.add(coche2);




        concesionarioMarshalling.setCoches(coches);
        System.out.println("=== Marshalling ===");
        System.out.println("Concesionario marshalling: " + concesionarioMarshalling.getCoches());
*/
    }
}
