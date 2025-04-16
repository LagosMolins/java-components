package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

public class GenericCoapResourceHandler extends CoapResource {
    private static final Logger _Logger = 
        Logger.getLogger(GenericCoapResourceHandler.class.getName());
    
    protected IDataMessageListener dataMsgListener;
    protected ResourceNameEnum resourceEnum;

    // Constructor principal ampliado
    public GenericCoapResourceHandler(String resourceName, 
                                    ResourceNameEnum resourceEnum, 
                                    IDataMessageListener listener) {
        super(resourceName);
        this.resourceEnum = resourceEnum;
        this.dataMsgListener = listener;
        
        _Logger.info("Created handler for resource: " + resourceName);
    }

    // Constructor alternativo (para compatibilidad)
    public GenericCoapResourceHandler(ResourceNameEnum resource) {
        this(resource.getResourceName(), resource, null);
    }

    // Constructor básico (para compatibilidad)
    public GenericCoapResourceHandler(String resourceName) {
        this(resourceName, null, null);
    }

    @Override
    public void handleDELETE(CoapExchange context) {
        context.accept();
        context.respond(ResponseCode.METHOD_NOT_ALLOWED, "DELETE not supported");
    }

    @Override
    public void handleGET(CoapExchange context) {
        context.accept();
        context.respond(ResponseCode.CONTENT, "GET: Use PUT to update data");
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

    public void setDataMessageListener(IDataMessageListener listener) {
        this.dataMsgListener = listener;
    }
}