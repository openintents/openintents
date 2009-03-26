// Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS.spi;


/**
 * The descriptor class for the dnsjava name service provider.
 * 
 * @author Brian Wellington
 * @author Paul Cowan (pwc21@yahoo.com)
 */

public class DNSJavaNameServiceDescriptor {

	private static DNSJavaNameService nameService;

	static {
		nameService = new DNSJavaNameService();
	}

	/**
	 * Returns a reference to a dnsjava name server provider.
	 */
	public DNSJavaNameService createNameService() {
		return nameService;
	}

	public String getType() {
		return "dns";
	}

	public String getProviderName() {
		return "dnsjava";
	}

}
