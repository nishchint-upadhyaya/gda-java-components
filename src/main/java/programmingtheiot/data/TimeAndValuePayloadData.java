/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2025 by Andrew D. King
 */

package programmingtheiot.data;

import java.io.Serializable;

import programmingtheiot.common.ConfigConst;

/**
 * Implementation of a minimum payload data container.
 * This class contains only a float value and long timestamp
 * (which is intended to represent milliseconds), along with
 * their respective accessor methods.
 * 
 * This class is intended for use with external services
 * that can process only a value and timestamp.
 * 
 * NOTE: This is NOT type compatible with BaseIotData and
 * its derivatives.
 *
 */
public class TimeAndValuePayloadData implements Serializable
{
	// static
	
	
	// private var's
	
	private float value = ConfigConst.DEFAULT_VAL;
    private long  timestamp = 0L;

	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public TimeAndValuePayloadData()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * Convenience constructor used for copying the value and milliseconds-based
	 * timestamp from a SensorData instance to this object's locally scoped vars.
	 */
	public TimeAndValuePayloadData(SensorData data)
	{
		super();

		if (data != null)
		{
			this.setTimeStampMillis(data.getTimeStampMillis());
			this.setValue(data.getValue());
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * Convenience constructor used for copying the value and milliseconds-based
	 * timestamp from an ActuatorData instance to this object's locally scoped vars.
	 */
	public TimeAndValuePayloadData(ActuatorData data)
	{
		super();

		if (data != null)
		{
			this.setTimeStampMillis(data.getTimeStampMillis());
			this.setValue(data.getValue());
		}
	}
	
	
	// public methods
	
	public long getTimeStampMillis()
	{
		return this.timestamp;
	}

	/**
	 * @return
	 */
	public float getValue()
	{
		return this.value;
	}
	
	/**
	 * 
	 * @param val
	 */
	public void setTimeStampMillis(long val)
	{
		this.timestamp = val;
	}

	/**
	 * 
	 * @param val
	 */
	public void setValue(float val)
	{
		this.value = val;
	}
	
	/**
	 * Returns a string representation of this instance. This will invoke the base class
	 * {@link #toString()} method, then append the output from this call.
	 * 
	 * @return String The string representing this instance, returned in CSV 'key=value' format.
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		
		sb.append(',');
		sb.append(ConfigConst.VALUE_PROP.toLowerCase()).append('=').append(this.getValue());
		sb.append(ConfigConst.TIMESTAMP_PROP.toLowerCase()).append('=').append(this.getTimeStampMillis());
		
		return sb.toString();
	}
	
	
	// protected methods
	
	
}
