package pl.umk.mat.odyn.main_service.types;


import android.util.Log;

/**
 * klasa ta powstała w celach DEBUGOWANIA. Nie wnosi nic do gotowej aplikacji.
 */
public class ServCounter {
	private static final boolean enabled = true; // wyłącz tę funkcjonalność nie usuwając kodu
	private static final boolean print = true;
	private static int serviceCount = 0;
	private static int camCount = 0;

	// SERVICES
	public static int getServiceCount() {
		if(!enabled)
			messageInactive();
		return serviceCount;
	}
	public static void serviceStarted() {
		if(enabled) {
			ServCounter.serviceCount++;
			if(print)
				displayServicesCount();
		}
		else
			messageInactive();
	}
	public static void serviceStopped() {
		if(enabled) {
			ServCounter.serviceCount--;
			if(print)
				displayServicesCount();
		}
		else
			messageInactive();
	}
	private static void displayServicesCount() {
		Log.d("ServCounter", ">>> there is "+ serviceCount + " services running");
	}


	// CAMERAS
	public static int getCamCount() {
		if(!enabled)
			messageInactive();
		return camCount;
	}
	public static void camStarted() {
		if(enabled) {
			ServCounter.camCount++;
			if(print)
				displayCamsCount();
		}
		else
			messageInactive();
	}
	public static void camStopped() {
		if(enabled) {
			ServCounter.camCount--;
			if(print)
				displayCamsCount();
		}
		else
			messageInactive();
	}
	private static void displayCamsCount() {
		Log.d("ServCounter", ">>> there is "+ camCount + " CamAccesses in app");
	}

	private static void messageInactive() {
		Log.w("ServCounter", ">>> it's disabled bro");
	}
}
