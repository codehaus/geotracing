package org.walkandplay.client.phone;

/**
 * Observer for Net.
 */
public interface NetListener {

	public void onNetInfo(String theInfo);

	public void onNetError(String aReason, Throwable anException);

	public void onNetStatus(String aStatusMsg);
}
