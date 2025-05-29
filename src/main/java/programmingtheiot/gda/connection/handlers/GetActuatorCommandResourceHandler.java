package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.CoapResource;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.data.DataUtil;

public class GetActuatorCommandResourceHandler extends CoapResource implements IActuatorDataListener {

    private static final Logger _Logger = Logger.getLogger(GetActuatorCommandResourceHandler.class.getName());

    // Parámetros
    private ActuatorData actuatorData = null;

    // Constructor principal
    public GetActuatorCommandResourceHandler(String resourceName) {
        super(resourceName);
        super.setObservable(true);  // Hacer que el recurso sea observable
    }

    // Implementación del método de la interfaz IActuatorDataListener
    @Override
    public boolean onActuatorDataUpdate(ActuatorData data) {
        if (data != null && this.actuatorData != null) {
            this.actuatorData.updateData(data);  // Actualizar los datos del actuador

            // Notificar a todos los clientes conectados que los datos han cambiado
            super.changed();

            _Logger.fine("Actuator data updated for URI: " + super.getURI() + ": Data value = " + this.actuatorData.getValue());
            return true;
        }

        return false;
    }

    // Implementación del método handleGET
    @Override
    public void handleGET(CoapExchange context) {
        // Log para indicar que se ha recibido la solicitud GET
        _Logger.info("Received GET request for resource: " + super.getURI());

        // Aceptar la solicitud
        context.accept();

        // Convertir el ActuatorData a JSON utilizando DataUtil
        String jsonData = DataUtil.getInstance().actuatorDataToJson(this.actuatorData);

        // Enviar la respuesta con el código de respuesta y los datos en formato JSON
        context.respond(ResponseCode.CONTENT, jsonData);
    }

    // Métodos adicionales para manejar PUT y DELETE pueden ser implementados si es necesario
    @Override
    public void handleDELETE(CoapExchange context) {
        context.accept();
        context.respond(ResponseCode.METHOD_NOT_ALLOWED, "DELETE not supported");
    }

    @Override
    public void handlePOST(CoapExchange context) {
        context.accept();
        context.respond(ResponseCode.METHOD_NOT_ALLOWED, "POST not supported");
    }

    @Override
    public void handlePUT(CoapExchange context) {
        context.accept();
        context.respond(ResponseCode.METHOD_NOT_ALLOWED, "PUT must be implemented in child class");
    }

}

