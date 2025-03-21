package programmingtheiot.data;

import com.google.gson.Gson;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;


public class DataUtil {
    // static
    private static final Logger _Logger = Logger.getLogger(DataUtil.class.getName());
    private static final DataUtil _Instance = new DataUtil();
    private static final Gson gson = new Gson(); // Reutilizar una única instancia de Gson

    /**
     * Returns the Singleton instance of this class.
     * 
     * @return DataUtil
     */
    public static final DataUtil getInstance() {
        return _Instance;
    }

    // constructors
    private DataUtil() {
        super();
    }

    // public methods
    public String actuatorDataToJson(ActuatorData data) {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
        }
        return jsonData;
    }

    public ActuatorData jsonToActuatorData(String jsonData) {
        ActuatorData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            try {
                data = gson.fromJson(jsonData, ActuatorData.class);
            } catch (Exception e) {
                _Logger.log(Level.SEVERE, "Error converting JSON to ActuatorData", e);
            }
        }
        return data;
    }

    public String sensorDataToJson(SensorData data) {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
        }
        return jsonData;
    }

    public SensorData jsonToSensorData(String jsonData) {
        SensorData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            try {
                data = gson.fromJson(jsonData, SensorData.class);
            } catch (Exception e) {
                _Logger.log(Level.SEVERE, "Error converting JSON to SensorData", e);
            }
        }
        return data;
    }

    public String systemPerformanceDataToJson(SystemPerformanceData data) {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
        }
        return jsonData;
    }

    public SystemPerformanceData jsonToSystemPerformanceData(String jsonData) {
        SystemPerformanceData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            try {
                data = gson.fromJson(jsonData, SystemPerformanceData.class);
            } catch (Exception e) {
                _Logger.log(Level.SEVERE, "Error converting JSON to SystemPerformanceData", e);
            }
        }
        return data;
    }

    public String systemStateDataToJson(SystemStateData data) {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
        }
        return jsonData;
    }

    public SystemStateData jsonToSystemStateData(String jsonData) {
        SystemStateData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            try {
                data = gson.fromJson(jsonData, SystemStateData.class);
            } catch (Exception e) {
                _Logger.log(Level.SEVERE, "Error converting JSON to SystemStateData", e);
            }
        }
        return data;
    }
}
