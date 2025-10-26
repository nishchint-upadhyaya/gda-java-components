/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * You may find it more helpful to your design to adjust the
 * functionality, constants and interfaces (if there are any)
 * provided within in order to meet the needs of your specific
 * Programming the Internet of Things project.
 */

package programmingtheiot.gda.connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.elements.config.UdpConfig;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
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
	
	static {
		CoapConfig.register();
		UdpConfig.register();
	}

	private static final Logger _Logger =
		Logger.getLogger(CoapServerGateway.class.getName());
	
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
		
		/*
		 * Basic constructor implementation provided. Change as needed.
		 */
		
		this.dataMsgListener = dataMsgListener;
		
		initServer();
	}

		
	// public methods
	
	public void addResource(ResourceNameEnum resourceType, String endName, Resource resource)
	{
		// TODO: while not needed for this exercise, you may want to include
		// the endName parameter as part of this resource chain creation process
		
		if (resourceType != null && resource != null) {
			// break out the hierarchy of names and build the resource
			// handler generation(s) as needed, checking if any parent already
			// exists - and if so, add to the existing resource

			CoapResource top =
				new CoapResource("PIOT").add(
					new CoapResource("ConstrainedDevice").add(
						new UpdateSystemPerformanceResourceHandler("SystemPerfMsg")));

			createAndAddResourceChain(resourceType, top);
		}
	}
	
	public boolean hasResource(String name)
	{
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
		return null;
	}
	
	private void initServer(ResourceNameEnum ...resources)
	{
		this.coapServer = new CoapServer();
		
		initDefaultResources();
	}

	private void initDefaultResources()
	{
		// initialize pre-defined resources
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
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null,	updateTelemetryResourceHandler);
		
		UpdateSystemPerformanceResourceHandler updateSystemPerformanceResourceHandler =
			new UpdateSystemPerformanceResourceHandler(
				ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE.getResourceType());
		
		updateSystemPerformanceResourceHandler.setDataMessageListener(this.dataMsgListener);
		
		CoapResource top =
			    new CoapResource("PIOT").add(
			        new CoapResource("ConstrainedDevice").add(
			            new UpdateSystemPerformanceResourceHandler("SystemPerfMsg")));
		this.coapServer.add(top);
		
		addResource(
			ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, null, updateSystemPerformanceResourceHandler);
	}

	private void createAndAddResourceChain(ResourceNameEnum resourceType, Resource resource)
	{
		_Logger.info("Adding server resource handler chain: " + resourceType.getResourceName());
		
		List<String> resourceNames = resourceType.getResourceNameChain();
		Queue<String> queue = new ArrayBlockingQueue<>(resourceNames.size());
		
		queue.addAll(resourceNames);
		
		// check if we have a parent resource
		Resource parentResource = this.coapServer.getRoot();
		
		// if no parent resource, add it in now (should be named "PIOT")
		if (parentResource == null) {
			parentResource = new CoapResource(queue.poll());
			this.coapServer.add(parentResource);
		}
		
		while (! queue.isEmpty()) {
			// get the next resource name
			String   resourceName = queue.poll();
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
