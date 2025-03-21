/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 
package programmingtheiot.gda.system;

import java.io.File;
import programmingtheiot.common.ConfigConst;
import java.util.logging.Logger;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemDiskUtilTask extends BaseSystemUtilTask
{
    // constructors
    
    /**
     * Default.
     * 
     */
    public SystemDiskUtilTask(){
        super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
    }

    // public methods
    
    @Override
    public float getTelemetryValue()
    {
        File file = new File("/"); // Root directory or specify a specific path
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();
        
        if (totalSpace == 0) {
            System.out.println("Disk Util not supported or unable to retrieve data.");
            return -1.0f;
        }
        
        float diskUtil = ((float)(totalSpace - freeSpace) / totalSpace) * 100;
        return diskUtil;
    }

    public static void main(String[] args) {
        // Crear instancia de la clase
        SystemDiskUtilTask task = new SystemDiskUtilTask();

        // Realizar múltiples pruebas para simular los Test 1, Test 2, etc.
        for (int i = 1; i <= 5; i++) {
            System.out.println("Test " + i + ": Disk Util " + task.getTelemetryValue() + "%");
        }
    }
}
