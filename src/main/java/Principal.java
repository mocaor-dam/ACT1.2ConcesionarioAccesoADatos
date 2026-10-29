import exceptions.MiEntradaSalidaException;
import jakarta.xml.bind.*;
import model.Coche;
import model.Concesionario;
import model.Equipamiento;
import utils.MiEntradaSalida;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Principal {
    static void main(String[] args) throws JAXBException {

        try {
            boolean activo = true;
            while (activo){
                int opcion = MiEntradaSalida.leerEnteroRango("Introduce la opcion: ", 1, 9);

                switch (opcion){
                    case 1:

                        break;
                    case 2:
                        String matriculaUsuario = MiEntradaSalida.leerTexto("Introduce la matricula: ");
                        String marcaUsuario = MiEntradaSalida.leerTexto("Introduce la matricula: ");
                        String modeloUsuario = MiEntradaSalida.leerTexto("Introduce el modelo: ");

                        
                        int numEquipamientos = MiEntradaSalida.leerEntero("Introduce cuantos modulos de equipamientos quieres añadir: ");

                       List<Equipamiento> equipamientosUsuario = new ArrayList<>(numEquipamientos);


                        
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                    case 9:
                        activo = false;
                        break;
                }
            }

        } catch (MiEntradaSalidaException e) {
            System.out.println(e.getMessage());
        }


    }


    public static void interfaz(){
        System.out.println("\n=== Menu Gestion Concesionario ===");
        System.out.println("1. Importar coches desde el CSV");
        System.out.println("2. Insertar un coche nuevo");
        System.out.println("3. Ordenar los coches por matricula");
        System.out.println("4. Borrar un registro");
        System.out.println("5. Modificar un registro");
        System.out.println("6. Exportar a JSON");
        System.out.println("7. Importar desde JSON");
        System.out.println("8. Generar informa resumen");
        System.out.println("9. Salir");
    }




}
