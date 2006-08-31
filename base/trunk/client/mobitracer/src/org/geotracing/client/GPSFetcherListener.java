// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

/**
 * Observer for GPSFetcher.
 */
public interface GPSFetcherListener {
	public void onGPSConnect();

	public void onGPSLocation(GPSLocation aLocation);

	public void onGPSInfo(GPSInfo theInfo);

	public void onGPSDisconnect();

	public void onGPSError(String aReason, Throwable anException);

	public void onGPSStatus(String aStatusMsg);

	public void onGPSTimeout();

}
