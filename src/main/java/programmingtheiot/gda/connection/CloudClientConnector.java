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

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Shell representation of class for student implementation.
 *
 */
public class CloudClientConnector implements ICloudClient
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnector.class.getName());
	
	// private var's
	
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public CloudClientConnector()
	{
		super();
		
	}
	
	
	// public methods
	
	@Override
	public boolean connectClient()
	{
		return false;
	}

	@Override
	public boolean disconnectClient()
	{
		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
	{
		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
	{
		return false;
	}

	@Override
	public boolean subscribeToCloudEvents(ResourceNameEnum resource)
	{
		return false;
	}

	@Override
	public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
	{
		return false;
	}
	
	
	// private methods
	
	
}
