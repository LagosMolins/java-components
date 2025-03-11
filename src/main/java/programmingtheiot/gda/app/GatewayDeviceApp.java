/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * You may find it more helpful to your design to adjust the
 * functionality, constants and interfaces (if there are any)
 * provided within in order to meet the needs of your specific
 * Programming the Internet of Things project.
 */ 
package programmingtheiot.gda.app;

import programmingtheiot.gda.system.SystemPerformanceManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GatewayDeviceApp {
    // Logger para registrar los mensajes
    private static final Logger logger = Logger.getLogger(GatewayDeviceApp.class.getName());

    private SystemPerformanceManager sysPerfMgr = null;

    // Constructor que acepta un parámetro de tipo String[]
    public GatewayDeviceApp(String[] args) {
        super();

        logger.info("Initializing GatewayDeviceApp...");

        this.sysPerfMgr = new SystemPerformanceManager();
    }

    // Método público que maneja la parada de la aplicación (sin usar System.exit())
    public void stopApp(int code) {
        logger.info("Stopping GDA...");

        try {
            if (this.sysPerfMgr.stopManager()) {
                logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
            } else {
                logger.warning("Failed to stop system performance manager!");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to cleanly stop GDA.", e);
        }

        // En lugar de System.exit(), solo se registra el código de salida
        logger.info("GDA stopped successfully. Exit code: " + code);
    }

    // Método público que maneja el inicio de la aplicación
    public void startApp() {
        logger.info("Starting GDA...");

        try {
            if (this.sysPerfMgr.startManager()) {
                logger.info("GDA started successfully.");
            } else {
                logger.warning("Failed to start system performance manager!");
                stopApp(-1);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
            stopApp(-1);
        }
    }

    // Método privado para inicializar la configuración (por ahora vacío)
    private void initConfig(String fileName) {
        logger.info("Initializing configuration with file: " + fileName);

        // Aquí se pueden agregar detalles adicionales para cargar el archivo de configuración
        // Como está vacío, por ahora no hay ninguna acción.
    }

    // Método privado para analizar los argumentos
    private void parseArgs(String[] args) {
        logger.info("Parsing arguments...");

        // Aquí se puede agregar la lógica para procesar los argumentos (si es necesario)
        // Por ahora, solo llamamos a initConfig con null
        initConfig(null);
    }

    // Método principal para ejecutar la aplicación
    public static void main(String[] args) {
        // Crear una instancia de GatewayDeviceApp
        GatewayDeviceApp app = new GatewayDeviceApp(args);

        // Llamar al método startApp() para iniciar la aplicación
        app.startApp();

        // Esperar 65 segundos
        try {
            Thread.sleep(65000);
        } catch (InterruptedException e) {
            logger.severe("Error occurred while waiting: " + e.getMessage());
        }

        // Llamar al método stopApp con código 0 después de 65 segundos
        app.stopApp(0);
    }
}
