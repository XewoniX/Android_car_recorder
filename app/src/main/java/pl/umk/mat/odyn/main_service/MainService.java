/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.main_service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import pl.umk.mat.odyn.gps.GPSThread;
import pl.umk.mat.odyn.gps.TimeHandlerThread;
import pl.umk.mat.odyn.main_service.types.IntentProvider;
import pl.umk.mat.odyn.main_service.types.ServCounter;
import pl.umk.mat.odyn.settings.SettingsProvider;
import pl.umk.mat.odyn.activities.MainScreen;
import pl.umk.mat.odyn.cam.Cam;
import pl.umk.mat.odyn.main_service.types.IconType;

// ta klasa Service będzie służyć do zapisu/odczytu obrazu oraz zajęć pobocznych, jak powiadomienia pływające

// TODO przerób na Foreground Service
/**
 * Jest to klasa Service odpowiedzialna za zapis/odczyt obrazu i powiadomień pobocznych.
 * Wykonuje zadania niezwiązane z UI, działa także, gdy aplikacja nie wyświetla się na ekranie.
 */
public class MainService extends Service {
	private Notification notif;
	private Cam cam; // dostęp do kamery

	private GPSThread gpsThread;
	private TimeHandlerThread timeHandlerThread;

	// nagrywanie przeniesione tutaj. gdy potrzeba zdjęcia, MainScreen (Activity) może wołać tę klasę

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	} // ZWRACA NULL. Za pomocą zwracanego IBinder powinno dać się komunikować z Service'm, póki co ZWRACA NULL

	/**
	 * Jest to metoda wywoływana przy uruchomieniu MainService.
	 * @param intent intencja
	 * @param flags flagi
	 * @param startId początkowe ID
	 */
	@Override
	public synchronized int onStartCommand(Intent intent, int flags, int startId) {
		//return super.onStartCommand(intent, flags, startId);

		if(intent != null) {
			if(intent.hasExtra(IntentProvider.extraName)) {
				IconType recvType = (IconType) intent.getSerializableExtra(IntentProvider.extraName);
				buttonHandler(recvType);
				Log.v("MainService", ">>> recieved action call");
			}
		 	else if(intent.hasExtra("start")) {
				// start serwisu, uruchomienie
				if(intent.getIntExtra("start", 0) != 1) {
					Log.w("MainService", ">>> MainService started with wrong value in intent!");
				}
				mainServiceStart();

				// utwórz MainScreen
				startMainScreen();

				// utwórz powiadomienie
				foregroundStarter();

				Log.v("MainService", ">>> MainService started");
			}
		}
		return START_STICKY; // uruchomienie / wyłączenie serwisu, tylko gdy się tego zażąda
	}

	/**
	 Jest to metoda służąca do ustawiania parametrów MainService.
	 */
	private void mainServiceStart() { // ma się wykonywać pokolei
		Log.v("MainService", ">>> setting up MainService");

		new SettingsProvider().loadSettings(this); // wczyta ustawienia

		// KOLEJNOŚĆ TWORZENIA WERSJA 1

		ServiceConnector.setOnClickHandle(this::buttonHandler);
		ServiceConnector.setCamReceiver(this::receiveCam); // MainScreen dostarczy Cam

		// utwórz Cam, trzeba dostarczyć do konstruktora MainScreen Activity
		//cam = new Cam(ServiceConnector.getActivity(), ServiceConnector.getActivity());
		// tutaj nie działa, cam musi być utworzony w głównym wątku
		// OD TERAZ: Cam tworzone w MainScreen.createCam() i przekazywane tu do setCam()
	}

	/**
	 Jest to metoda służąca do uruchamiania głównego ekranu aplikacji.
	 */
	private void startMainScreen() {
		// DrawerActivity zawiera MainScreen, więc ok.
		Intent startMainScreen = new Intent(this, MainScreen.class);
		startMainScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMainScreen);
	}

	/**
	 * Jest to metoda odpowiedzialna za uruchamianie serwisu w trybie Foreground, tworzy powiadomienie
	 */
	private void foregroundStarter() {
		//
		int ajdi = 5; // trzeba sprawdzić, TEMPORARY
		NotificationCreator creator = new NotificationCreator(this);
		startForeground(ajdi, creator.create());
	}


	// tu obsłuż przyciski, te powiązane z wideo przekaż do Cam
	/**
	 * Jest to metoda służąca do obsługiwania przycisków aplikacji.
	 * @param it Typ ikony
	 */
	private void buttonHandler(IconType it) {
		Log.d("MainService", ">>> ikonka akcji "+ it);
		// niestety, android jest oparty starym JDK i nie ma enchanced switch'a
		switch(it) {
			// bloki switch case działają jak go to label, więc dozwolone.
			case photo:
			case recording:
			case emergency:
				cam.camAction(it);
				break;
			case close:
				// zamknij apkę
				Log.d("MainService", ">>> zamykam aplikację");
				//startActivity(IntentProvider.iconClicked(this, IconType.close));
				break;
			case menu:
				// nottodo: MainScreen będzie otwierać menu
				break;
			case back_to_app:
				Log.d("MainService", ">>> powrót do aplikacji");
				startMainScreen(); // ? należy otworzyć MainScreen, a to tworzy. Można tak???
				break;
			case display_notif:
				// nieaktywne, Service wersja 1
				Log.wtf("MainService", ">>> What, display_notif? Powiadomienie widoczne cały czas");
				break;
			case hide_notif:
				// nieaktywne, Service wersja 1
				Log.wtf("MainService", ">>> What, hide_notif? Powiadomienie widoczne cały czas");
				break;
			default:
				Log.wtf("MainService", ">>> What? ikonka "+ it);
		}
	}

	// użytkownik wyszedł z aplikacji nie zamykając jej. wyświetl powiadomienie, że aplikacja nadal nagrywa. wywołać w MainScreen.onStop()
	/**
	 Jest to metoda służąca do wyświetlania powiadomienia o nagrywaniu aplikacji, gdy użytkownik wyjdzie z aplikacji nie zamykając jej.
	 */
	public void appNotOnScreen() {
		notif = new NotificationCreator(this).create();
	}

	// użytkownik wszedł z powrotem do aplikacji. zamknij powiadomienie. wywołać w MainScreen.onRestart()
	/**
	 Jest to metoda służąca do zamykania powiadomienia o nagrywaniu aplikacji, gdy użytkownik wejdzie ponownie do aplikacji.
	 */
	public void appBackOnScreen() {
		//
		// zamknij powiadomienie ???
	}

	/**
	 Jest to metoda służąca do tworzenia pól i metod klasy MainService.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		gpsThread = new GPSThread(getApplicationContext());
		gpsThread.start();
		//Log.d("MainService/GPSThread", "Started GPSThread");
		timeHandlerThread = new TimeHandlerThread();
		timeHandlerThread.start();
		ServCounter.serviceStarted();
		Log.v("MainService", ">>> MainService created");
	}

	/**
	 Jest to metoda służąca do usuwania pozostałości klasy MainService po wyjściu z aplikacji.
	 */
	@Override
	public void onDestroy() {
		Log.v("MainService", ">>> MainService destroyed");
		ServCounter.serviceStopped();
		gpsThread.stopGPS();
		timeHandlerThread.stopTimer();
		cam.stopSRT();
		Log.d("MainService", ">>> onDestroy, ilość kamer: " + ServCounter.getCamCount());
		super.onDestroy();
	}

	/**
	 Jest to metoda służąca do otrzymywania kamery.
	 @param cam Obiekt klasy Cam
	 */
	private void receiveCam(Cam cam) {
		this.cam = cam;
	}

}