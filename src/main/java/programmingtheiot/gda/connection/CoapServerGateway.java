/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

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
 
 /**
  * Shell representation of class for student implementation.
  * 
  */
 public class CoapServerGateway
 {
	 // static
	 
	 private static final Logger _Logger =
		 Logger.getLogger(CoapServerGateway.class.getName());
	 
	 // Static initializer for Californium > 3.8.0
	 static {
		 CoapConfig.register();
		 UdpConfig.register();
	 }
	 
	 // params
	 
	 private CoapServer coapServer = null;
	 
	 private IDataMessageListener dataMsgListener = null;
	 
	 
	 // constructors
	 
	 /**
	  * Constructor.
	  * 
	  * @param dataMsgListener
	  */
	 public CoapServerGateway(IDataMessageListener dataMsgListener)
	 {
		 super();
		 
		 this.dataMsgListener = dataMsgListener;
		 
		 initServer();
	 }

	 
	 private void initServer(ResourceNameEnum ...resources)
	 {
		 this.coapServer = new CoapServer();
		 
		 // Add default resources if any are specified
		 if (resources != null && resources.length > 0) {
			 for (ResourceNameEnum resource : resources) {
				 addResource(resource);
			 }
		 }
		 
		 _Logger.info("CoAP server initialized. Resources: " + 
			 (resources != null ? resources.length : 0));
	 }
 
		 
	 // public methods
	public void addResource(ResourceNameEnum resource) {
		if (resource != null && this.coapServer != null) {
			CoapResource handler = null;
			
			switch(resource) {
				case CDA_SYSTEM_PERF_MSG_RESOURCE:
					handler = new UpdateSystemPerformanceResourceHandler(resource.getResourceName());
					break;
				case CDA_SENSOR_MSG_RESOURCE:
					handler = new UpdateTelemetryResourceHandler(resource.getResourceName());
					break;
				default:
					handler = new GenericCoapResourceHandler(resource.getResourceName());
			}
			
			if (handler != null) {
				((GenericCoapResourceHandler)handler).setDataMessageListener(this.dataMsgListener);
				this.coapServer.add(handler);
			}
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
	 
	 private Resource createResourceChain(ResourceNameEnum resource)
	 {
		 if (resource == null) {
			 return null;
		 }
 
		 String resourceName = resource.getResourceName();
		 if (resourceName == null || resourceName.trim().length() == 0) {
			 return null;
		 }
 
		 String[] parts = resourceName.split("/");
		 if (parts == null || parts.length == 0) {
			 return null;
		 }
 
		 CoapResource rootResource = new GenericCoapResourceHandler(parts[0], resource, this.dataMsgListener);
		 CoapResource currentResource = rootResource;
 
		 for (int i = 1; i < parts.length; i++) {
			 CoapResource newResource = new GenericCoapResourceHandler(parts[i], resource, this.dataMsgListener);
			 currentResource.add(newResource);
			 currentResource = newResource;
		 }
 
		 return rootResource;
	 }
	 
	 
 }