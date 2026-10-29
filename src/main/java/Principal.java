import exceptions.AppException;
import exceptions.MatriculaRepetidaException;
import exceptions.MiEntradaSalidaException;
import jakarta.xml.bind.*;
import model.Coche;
import model.Concesionario;
import model.Equipamiento;
import service.GestionConcesionarioService;
import utils.MiEntradaSalida;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Principal {
    public static void main(String[] args) throws JAXBException {


        try {
            GestionConcesionarioService.inicializar();
            System.out.println("Se ha cargado la base de datos correctamente");
        } catch (AppException e) {
            System.out.println("Error al iniciar la base de datos: " + e.getMessage());
        }

        boolean activo = true;
        while (activo) {
            try {
                interfaz();
                int opcion = MiEntradaSalida.leerEnteroRango("Introduce la opcion: ", 1, 9);

                switch (opcion) {
                    case 1:
                        GestionConcesionarioService.importarDesdeCsv();
                        System.out.println("Se han importado correctamente los coches desde el archivo CSV.");
                        break;
                    case 2:
                        //Recogemos los datos del coche
                        String matriculaUsuario = MiEntradaSalida.leerTexto("Introduce la matricula: ");
                        String marcaUsuario = MiEntradaSalida.leerTexto("Introduce la marca: ");
                        String modeloUsuario = MiEntradaSalida.leerTexto("Introduce el modelo: ");


                        int numEquipamientos = MiEntradaSalida.leerEntero("Introduce cuantos modulos de equipamientos quieres añadir: ");

                        //Creamos una lista de los equipamientos
                        List<Equipamiento> equipamientosUsuario = new ArrayList<>(numEquipamientos);


                        for (int i = 0; i < numEquipamientos; i++) {
                            String extra = MiEntradaSalida.leerTexto("Introduce el equipamiento: ");
                            Equipamiento e = new Equipamiento();
                            e.setNombre(extra);
                            equipamientosUsuario.add(e);
                        }

                        Coche coche = new Coche();

                        coche.setMatricula(matriculaUsuario);
                        coche.setMarca(marcaUsuario);
                        coche.setModelo(modeloUsuario);
                        coche.setEquipamientos(equipamientosUsuario);

                        GestionConcesionarioService.insertarCocheNuevo(coche);

                        System.out.println("Coche insertado correctamente.");

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
            } catch (MiEntradaSalidaException | AppException | MatriculaRepetidaException e) {
                System.out.println(e.getMessage());

            }
        }


    }


    public static void interfaz() {
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
