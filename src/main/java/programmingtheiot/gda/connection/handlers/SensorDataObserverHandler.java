package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import programmingtheiot.common.IDataMessageListener;

public class SensorDataObserverHandler implements CoapHandler
{
	private static final Logger _Logger =
		Logger.getLogger(SensorDataObserverHandler.class.getName());
	
    private IDataMessageListener dataMsgListener;
    
    public void setDataMessageListener(IDataMessageListener listener)
    {
        this.dataMsgListener = listener;
    }

	public SensorDataObserverHandler ()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.californium.core.CoapHandler#onError()
	 */
	public void onError()
	{
		_Logger.warning("Handling CoAP error...");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.californium.core.CoapHandler#onLoad(org.eclipse.californium.core.CoapResponse)
	 */
	public void onLoad(CoapResponse response)
	{
		_Logger.info("Received CoAP response (payload should be SensorData in JSON): " + response.getResponseText());
	}
}
