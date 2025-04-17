package programmingtheiot.gda.connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.Resource;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;
import programmingtheiot.gda.connection.handlers.GetActuatorCommandResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateSystemPerformanceResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateTelemetryResourceHandler;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class CoapServerGateway
{
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(CoapServerGateway.class.getName());
    
    // params
    
    private CoapServer coapServer = null;
    private IDataMessageListener dataMsgListener = null;
    private boolean isInitialized = false;
    
    // constructors
    
    public CoapServerGateway(IDataMessageListener dataMsgListener)
    {
        super();
        
        this.dataMsgListener = dataMsgListener;
        
        initServer();
    }

    // public methods
    
    public void addResource(ResourceNameEnum resourceType, String endName, Resource resource)
    {
        if (resourceType != null && resource != null) {
            createAndAddResourceChain(resourceType, resource);
        }
    }
    
    public boolean hasResource(String name)
    {
        if (name != null && this.coapServer != null) {
            return this.coapServer.getRoot().getChild(name) != null;
        }
        
        return false;
    }
    
    public void setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }
    
    public boolean startServer()
    {
        try {
            if (this.coapServer != null && !this.isInitialized) {
                initDefaultResources();
                this.isInitialized = true;
            }
            
            if (this.coapServer != null) {
                this.coapServer.start();

                // for message logging
                for (Endpoint ep : this.coapServer.getEndpoints()) {
                    ep.addInterceptor(new MessageTracer());
                }

                _Logger.info("CoAP server started successfully.");
                return true;
            } else {
                _Logger.warning("CoAP server START failed. Not yet initialized.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to start CoAP server.", e);
        }

        return false;
    }
    
    public boolean stopServer()
    {
        try {
            if (this.coapServer != null) {
                this.coapServer.stop();
                _Logger.info("CoAP server stopped successfully.");
                return true;
            } else {
                _Logger.warning("CoAP server STOP failed. Not yet initialized.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to stop CoAP server.", e);
        }

        return false;
    }
    
    // private methods
    
    private void initServer()
    {
        if (this.coapServer == null) {
            this.coapServer = new CoapServer();
            _Logger.info("CoAP server instance created.");
        }
    }
    
    private void initDefaultResources()
    {
        // Initialize pre-defined resources
        GetActuatorCommandResourceHandler getActuatorCmdResourceHandler = 
            new GetActuatorCommandResourceHandler(
                ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE.getResourceType());

        if (this.dataMsgListener != null) {
            this.dataMsgListener.setActuatorDataListener(null, getActuatorCmdResourceHandler);
        }

        addResource(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, null, getActuatorCmdResourceHandler);

        UpdateTelemetryResourceHandler updateTelemetryResourceHandler = 
            new UpdateTelemetryResourceHandler(
                ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE.getResourceType());

        updateTelemetryResourceHandler.setDataMessageListener(this.dataMsgListener);

        addResource(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, updateTelemetryResourceHandler);

        UpdateSystemPerformanceResourceHandler updateSystemPerformanceResourceHandler = 
            new UpdateSystemPerformanceResourceHandler(
                ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE.getResourceType());

        updateSystemPerformanceResourceHandler.setDataMessageListener(this.dataMsgListener);

        addResource(
            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, null, updateSystemPerformanceResourceHandler);
    }
    
    private void createAndAddResourceChain(ResourceNameEnum resourceType, Resource resource)
    {
        _Logger.info("Adding server resource handler chain: " + resourceType.getResourceName());

        List<String> resourceNames = resourceType.getResourceNameChain();
        Queue<String> queue = new ArrayBlockingQueue<>(resourceNames.size());

        queue.addAll(resourceNames);

        // Check if we have a parent resource
        Resource parentResource = this.coapServer.getRoot();

        // If no parent resource, add it in now (should be named "PIOT")
        if (parentResource == null) {
            parentResource = new CoapResource(queue.poll());
            this.coapServer.add(parentResource);
        }

        while (!queue.isEmpty()) {
            // Get the next resource name
            String resourceName = queue.poll();
            Resource nextResource = parentResource.getChild(resourceName);

            if (nextResource == null) {
                if (queue.isEmpty()) {
                    nextResource = resource;
                    nextResource.setName(resourceName);
                } else {
                    nextResource = new CoapResource(resourceName);
                }

                parentResource.add(nextResource);
            }

            parentResource = nextResource;
        }
    }
}