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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;


public class SmtpClientConnector
{
	// static
	
	
	
	// private var's
	
	
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SmtpClientConnector()
	{
		super();
	}
	
	
	// public methods
	
	public boolean sendMessage(ResourceNameEnum resource, String payload, int timeout)
	{
		return false;
	}

	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		return false;
	}

	
	// private methods
	
	
}
