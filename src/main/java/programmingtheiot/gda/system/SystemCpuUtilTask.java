/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 
package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import programmingtheiot.common.ConfigConst;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Logger;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemCpuUtilTask extends BaseSystemUtilTask
{
    // constructors
    
    /**
     * Default.
     * 
     */
    public SystemCpuUtilTask(){
        super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
    }

    // public methods
    
    @Override
    public float getTelemetryValue()
    {
        OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuUtil = mxBean.getSystemLoadAverage();

        // Verificar si el valor es -1.0 (no soportado)
        if (cpuUtil == -1.0) {
            System.out.println("CPU Util not supported on this OS: " + cpuUtil);
            return -1.0f;  // Devuelve -1.0f si no es compatible
        }
        
        // Si es un valor válido, devuelve el valor en tipo float
        return (float) cpuUtil;
    }

    public static void main(String[] args) {
        // Crear instancia de la clase
        SystemCpuUtilTask task = new SystemCpuUtilTask();

        // Realizar múltiples pruebas para simular los Test 1, Test 2, etc.
        for (int i = 1; i <= 5; i++) {
            System.out.println("Test " + i + ": CPU Util " + task.getTelemetryValue());
        }
    }
}

