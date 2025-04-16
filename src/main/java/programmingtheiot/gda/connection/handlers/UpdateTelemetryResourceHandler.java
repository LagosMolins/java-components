package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.DataUtil;

public class UpdateTelemetryResourceHandler extends CoapResource {
    // static
    private static final Logger _Logger = 
        Logger.getLogger(UpdateTelemetryResourceHandler.class.getName());
    
    // private
    private IDataMessageListener dataMsgListener = null;
    
    // constructors
    public UpdateTelemetryResourceHandler(String resourceName) {
        super(resourceName);
    }
    
    // public methods
    public void setDataMessageListener(IDataMessageListener listener) {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }
    
    @Override
    public void handleGET(CoapExchange context) {
        context.accept();
        String msg = "GET request handled: " + super.getName();
        _Logger.info(msg);
        context.respond(ResponseCode.CONTENT, msg);
    }
    
    @Override
    public void handlePOST(CoapExchange context) {
        context.accept();
        String msg = "POST request handled: " + super.getName();
        _Logger.info(msg);
        context.respond(ResponseCode.CREATED, msg);
    }
    
    @Override
    public void handlePUT(CoapExchange context) {
        ResponseCode code = ResponseCode.NOT_ACCEPTABLE;
        
        context.accept();
        
        if (this.dataMsgListener != null) {
            try {
                String jsonData = new String(context.getRequestPayload());
                SensorData sensorData = 
                    DataUtil.getInstance().jsonToSensorData(jsonData);
                
                this.dataMsgListener.handleSensorMessage(
                    ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
                
                code = ResponseCode.CHANGED;
            } catch (Exception e) {
                _Logger.warning(
                    "Failed to handle PUT request. Message: " + e.getMessage());
                code = ResponseCode.BAD_REQUEST;
            }
        } else {
            _Logger.info(
                "No callback listener for request. Ignoring PUT.");
            code = ResponseCode.CONTINUE;
        }
        
        String msg = "Update telemetry data request handled: " + super.getName();
        context.respond(code, msg);
    }
    
    @Override
    public void handleDELETE(CoapExchange context) {
        context.accept();
        String msg = "DELETE request handled: " + super.getName();
        _Logger.info(msg);
        context.respond(ResponseCode.DELETED, msg);
    }
}