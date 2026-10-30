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
                        String matriculaUsuario = MiEntradaSalida.leerLinea("Introduce la matricula: ");
                        String marcaUsuario = MiEntradaSalida.leerLinea("Introduce la marca: ");
                        String modeloUsuario = MiEntradaSalida.leerLinea("Introduce el modelo: ");


                        int numEquipamientos = MiEntradaSalida.leerEntero("Introduce cuantos modulos de equipamientos quieres añadir: ");

                        //Creamos una lista de los equipamientos
                        List<Equipamiento> equipamientosUsuario = new ArrayList<>(numEquipamientos);


                        for (int i = 0; i < numEquipamientos; i++) {
                            String extra = MiEntradaSalida.leerLinea("Introduce el equipamiento: ");
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
                        GestionConcesionarioService.ordenarPorMatricula();
                        System.out.println("Coches ordenados por matricula correctamente");
                        break;
                    case 4:
                        String matriculaABorrar = MiEntradaSalida.leerLinea("Introduce la matricula del coche que quieras borrar");
                        GestionConcesionarioService.borrarRegistroPorMatricula(matriculaABorrar);
                        System.out.println("El Coche con matricula: " + matriculaABorrar + " fue borrado correctamente");
                        break;
                    case 5:
                        String matriculaBuscar = MiEntradaSalida.leerLinea("Introduce la matricula del coche que quieras modificar");

                        String marcaNueva = MiEntradaSalida.leerLinea("Introduce la marca");
                        String modeloNuevo = MiEntradaSalida.leerLinea("Introduce la modelo");


                        int numEquipamientosNuevos = MiEntradaSalida.leerEntero("Introduce el numero de equipamientos que le quieras añadir");

                        List<Equipamiento> equipamientos = new ArrayList<>(numEquipamientosNuevos);

                        for (int i = 0; i < numEquipamientosNuevos; i++) {
                            String extraNuevo = MiEntradaSalida.leerLinea("Introduce el extra nuevo");

                            Equipamiento equipamientoNuevo = new Equipamiento();

                            equipamientoNuevo.setNombre(extraNuevo);

                            equipamientos.add(equipamientoNuevo);

                        }

                        GestionConcesionarioService.modificarRegistro(matriculaBuscar, marcaNueva, modeloNuevo, equipamientos);

                        break;
                    case 6:
                        System.out.println("\nTienes estas opciones: ");
                        System.out.println("1. Exportar toda la base de datos");
                        System.out.println("2. Exportar un solo coche");

                        int opcion6 = MiEntradaSalida.leerEnteroRango("Selecciona la opcion", 1, 2);

                        if (opcion6 == 1){
                            GestionConcesionarioService.exportarConcesionarioJson();
                            System.out.println("Base de Datos exportada a JSON correctamente.");
                        } else {
                            String matriculaBuscada = MiEntradaSalida.leerLinea("Introduce la matricula del coche que quieras exportar.");
                            String rutaSalida = MiEntradaSalida.leerLinea("Introduce la ruta o el nombre del fichero JSON al que exportar el coche.");
                            GestionConcesionarioService.exportarCocheJson(matriculaBuscada, rutaSalida);
                            System.out.println("Coche exportado a JSON correctamente");
                        }
                        break;
                    case 7:
                        System.out.println("1. Importar Base de datos");
                        System.out.println("2. Importar un coche");

                        int op7 = MiEntradaSalida.leerEnteroRango("Selecciona la opcion", 1,2);

                        if (op7 == 1){
                            String rutaJsonBBDD = MiEntradaSalida.leerLinea("Introduce la ruta del Json a importar");
                            GestionConcesionarioService.importarConcesionarioJson(rutaJsonBBDD);
                            System.out.println("Base de datos importada correctamente!");
                        } else {
                            String rutaJsonCoche = MiEntradaSalida.leerLinea("Introduce la ruta del fichero Json del que se va a importar el coche");
                            GestionConcesionarioService.importarCocheJson(rutaJsonCoche);
                            System.out.println("Coche importado correctamente!");
                        }
                        break;
                    case 8:
                        GestionConcesionarioService.generarInformeResumen();
                        break;
                    case 9:
                        activo = false;
                        break;
                }
            } catch (MiEntradaSalidaException | AppException | MatriculaRepetidaException | JAXBException e) {
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
        System.out.println("8. Generar informe resumen");
        System.out.println("9. Salir");
    }


}
