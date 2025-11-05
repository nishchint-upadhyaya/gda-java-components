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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResponseHandler;
import programmingtheiot.gda.connection.handlers.SensorDataObserverHandler;

/**
 * Shell representation of class for student implementation.
 *
 */
public class CoapClientConnector implements IRequestResponseClient
{
	// static
	
	private String     protocol;
	private String     host;
	private int        port;
	private String     serverAddr;
	private CoapClient clientConn;
	private IDataMessageListener dataMsgListener;

	private static final Logger _Logger =
		Logger.getLogger(CoapClientConnector.class.getName());
	
	// params
	
	
	// constructors
	
	/**
	 * Default.
	 * 
	 * All config data will be loaded from the config file.
	 */

	public CoapClientConnector()
	{
		ConfigUtil config = ConfigUtil.getInstance();
		this.host = config.getProperty(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);

		if (config.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
			this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
		} else {
			this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
		}
		// NOTE: URL does not have a protocol handler for "coap",
		// so we need to construct the URL manually
		this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;

		initClient();

		_Logger.info("Using URL for server conn: " + this.serverAddr);
	}
		
	/**
	 * Constructor.
	 * 
	 * @param host
	 * @param isSecure
	 * @param enableConfirmedMsgs
	 */
	public CoapClientConnector(String host, boolean isSecure, boolean enableConfirmedMsgs)
	{

	}
	
	
	// public methods
	
	@Override
	public boolean sendDiscoveryRequest(int timeout)
	{
		try {
			if (this.clientConn == null) {
				_Logger.warning("Client connection is not initialized.");
				return false;
			}

			this.clientConn.setURI("/.well-known/core");

			// TODO: implement your own Discovery-specific response handler if you'd like, using the parsing logic from Option 2
			GenericCoapResponseHandler responseHandler = new GenericCoapResponseHandler(this.dataMsgListener);
			this.clientConn.get(responseHandler);

			return true;
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to send discovery request", e);
			return false;
		}
	}

	@Override
	public boolean sendDeleteRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());

		// TODO: This is NOT a performance-savvy way to use a response handler, as it will require
		// creating a new response handler with every call to this method. This is AN EXAMPLE ONLY.
		// A better solution would involve creation of a resource and / or request type-specific
		// response handler at construction time and storing it in a class-scoped variable for
		// re-use in this call.
		CoapHandler responseHandler = new GenericCoapResponseHandler(this.dataMsgListener);
		this.clientConn.delete(responseHandler);

		// TODO: you may want to implement a unique, DELETE and resource-specific CoapHandler modeled after GenericCoapResponseHandler.

		return true;
	}

	@Override
	public boolean sendGetRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());

		// TODO: This is NOT a performance-savvy way to use a response handler, as it will require
		// creating a new response handler with every call to this method. This is AN EXAMPLE ONLY.
		// A better solution would involve creation of a resource and / or request type-specific
		// response handler at construction time and storing it in a class-scoped variable for
		// re-use in this call.
		CoapHandler responseHandler = new GenericCoapResponseHandler(this.dataMsgListener);
		this.clientConn.get(responseHandler);

		// TODO: you may want to implement a unique, GET and resource-specific CoapHandler modeled after GenericCoapResponseHandler.

		return true;
	}

	@Override
	public boolean sendPostRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());

		// TODO: This is NOT a performance-savvy way to use a response handler, as it will require
		// creating a new response handler with every call to this method. This is AN EXAMPLE ONLY.
		// A better solution would involve creation of a resource and / or request type-specific
		// response handler at construction time and storing it in a class-scoped variable for
		// re-use in this call.
		CoapHandler responseHandler = new GenericCoapResponseHandler(this.dataMsgListener);
		this.clientConn.post(responseHandler, payload, MediaTypeRegistry.TEXT_PLAIN);

		// TODO: you may want to implement a unique, POST and resource-specific CoapHandler modeled after GenericCoapResponseHandler.

		return true;
	}

	@Override
	public boolean sendPutRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());

		// TODO: This is NOT a performance-savvy way to use a response handler, as it will require
		// creating a new response handler with every call to this method. This is AN EXAMPLE ONLY.
		// A better solution would involve creation of a resource and / or request type-specific
		// response handler at construction time and storing it in a class-scoped variable for
		// re-use in this call.
		CoapHandler responseHandler = new GenericCoapResponseHandler(this.dataMsgListener);
		this.clientConn.put(responseHandler, payload, MediaTypeRegistry.TEXT_PLAIN);

		// TODO: you may want to implement a unique, PUT and resource-specific CoapHandler modeled after GenericCoapResponseHandler.

		return true;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null)
		{
			this.dataMsgListener = listener;
			return true;
		}

		return false;
	}

	public void clearEndpointPath()
	{
	}
	
	public void setEndpointPath(ResourceNameEnum resource)
	{
	}
	
	@Override
	public boolean startObserver(ResourceNameEnum resource, String name, int ttl)
	{
		String uriPath = createUriPath(resource, name);
		
		_Logger.info("Observing resource [START]: " + uriPath);
		
		this.clientConn.setURI(uriPath);
		
		// TODO: Check the resource type:
		//   - If it references SensorData, create the SensorDataObserverHandler
		//   - If it references SystemPerformanceData, create the SystemPerformanceDataObserverHandler
		SensorDataObserverHandler handler = new SensorDataObserverHandler();
		handler.setDataMessageListener(this.dataMsgListener);
		
		CoapObserveRelation cor = this.clientConn.observe(handler);
		
		// TODO: store a reference to the relation instance and map it to the resource under observation,
		// as it will be needed if the caller wants to cancel the observation at a later time
		
		return (! cor.isCanceled());
	}

	@Override
	public boolean stopObserver(ResourceNameEnum resourceType, String name, int timeout)
	{
		return false;
	}

	
// private methods

private String createUriPath(ResourceNameEnum resource, String name)
{
	if (resource == null) {
		return this.serverAddr;
	}
	
	StringBuilder sb = new StringBuilder();
	if (this.serverAddr != null && !this.serverAddr.isEmpty()) {
		sb.append(this.serverAddr);
	} else {
		String proto = this.protocol != null ? this.protocol : ConfigConst.DEFAULT_COAP_PROTOCOL;
		String h = this.host != null ? this.host : ConfigConst.DEFAULT_HOST;
		int p = this.port != 0 ? this.port : ConfigConst.DEFAULT_COAP_PORT;
		sb.append(proto).append("://").append(h).append(":").append(p);
	}
	
	sb.append("/").append(resource.getResourceName());
	
	if (name != null && !name.isEmpty()) {
		sb.append("/").append(name);
	}
	
	return sb.toString();
}

private void initClient()
{
	try {
		this.clientConn = new CoapClient(this.serverAddr);
		
		_Logger.info("Created client connection to server / resource: " + this.serverAddr);
	} catch (Exception e) {
		_Logger.log(Level.SEVERE, "Failed to connect to broker: " + (this.clientConn != null ? this.clientConn.getURI() : this.serverAddr), e);
	}
}
}
