package org.walkandplay.client.phone;

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
