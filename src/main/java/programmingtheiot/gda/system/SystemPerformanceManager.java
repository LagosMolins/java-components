/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 
/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 
package programmingtheiot.gda.system;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;

/**
 * SystemPerformanceManager manages the monitoring of system performance,
 * including CPU and memory utilization.
 */
public class SystemPerformanceManager
{
    // Logger for logging events
    private static final Logger logger = Logger.getLogger(SystemPerformanceManager.class.getName());

    // Polling rate for periodic tasks
    private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;

    // ScheduledExecutorService for handling periodic task execution
    private ScheduledExecutorService schedExecSvc = null;

    // Tasks for CPU and Memory utilization
    private SystemCpuUtilTask sysCpuUtilTask = null;
    private SystemMemUtilTask sysMemUtilTask = null;
    private SystemDiskUtilTask sysDiskUtilTask = null;
    private IDataMessageListener dataMsgListener;


    // Runnable task to be executed periodically
    private Runnable taskRunner = null;

    // Flag to check if the manager is already started
    private boolean isStarted = false;

    // Constructor
    public SystemPerformanceManager()
    {
        // Retrieve the poll rate from configuration
        this.pollRate = ConfigUtil.getInstance().getInteger(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.POLL_CYCLES_KEY, ConfigConst.DEFAULT_POLL_CYCLES);

        if (this.pollRate <= 0) {
            this.pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
        }

        // Initialize the scheduled executor service and the tasks
        this.schedExecSvc = Executors.newScheduledThreadPool(1);
        this.sysCpuUtilTask = new SystemCpuUtilTask();
        this.sysMemUtilTask = new SystemMemUtilTask();
        this.sysDiskUtilTask = new SystemDiskUtilTask();

        // Define the task to be executed periodically
        this.taskRunner = () -> {
            this.handleTelemetry();
        };
    }

    // Method to handle the telemetry for CPU and Memory utilization jhgkuy
    public void handleTelemetry()
    {
        // Retrieve CPU and memory utilization values
        float cpuUtil = this.sysCpuUtilTask.getTelemetryValue();
        float memUtil = this.sysMemUtilTask.getTelemetryValue();
        float diskUtil = this.sysDiskUtilTask.getTelemetryValue();

        // Log the utilization values
        logger.fine("CPU utilization: " + cpuUtil + "%, Mem utilization: " + memUtil + "%");
    }

    public void setDataMessageListener(IDataMessageListener listener) {
        this.dataMsgListener = listener;
}



    // Method to start the manager and begin scheduled task execution
    public boolean startManager()
    {
        if (!this.isStarted) {
            logger.info("SystemPerformanceManager is starting...");

            // Schedule the periodic execution of the task
            ScheduledFuture<?> futureTask =
                this.schedExecSvc.scheduleAtFixedRate(this.taskRunner, 1L, this.pollRate, TimeUnit.SECONDS);

            this.isStarted = true;
        } else {
            logger.info("SystemPerformanceManager is already started.");
        }

        return this.isStarted;
    }

    // Method to stop the manager and cancel the scheduled task
    public boolean stopManager()
    {
        // Shutdown the scheduled executor service
        this.schedExecSvc.shutdown();
        this.isStarted = false;

        logger.info("SystemPerformanceManager is stopped.");

        return true;
    }
}

