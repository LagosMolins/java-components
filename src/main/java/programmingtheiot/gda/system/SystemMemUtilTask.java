/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import java.util.logging.Logger;
import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemMemUtilTask extends BaseSystemUtilTask
{
	private static final Logger _Logger = Logger.getLogger(SystemMemUtilTask.class.getName());

	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemMemUtilTask()
	{
		super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
	}
	// public methods
	@Override
	public float getTelemetryValue()
	{
		// Obtener el uso de memoria del heap de la JVM
		MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		double memUsed = (double) memUsage.getUsed();  // Memoria utilizada
		double memMax  = (double) memUsage.getMax();   // Memoria máxima

		// Registrar la memoria utilizada y la memoria máxima
		_Logger.fine("Mem used: " + memUsed + "; Mem Max: " + memMax);

		// Calcular la utilización de memoria como un porcentaje
		double memUtil = (memUsed / memMax) * 100.0d;

		// Devolver el valor como un float
		return (float) memUtil;
	}
}
