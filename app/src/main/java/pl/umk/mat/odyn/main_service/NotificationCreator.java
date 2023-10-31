/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.main_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import pl.umk.mat.odyn.R;

import pl.umk.mat.odyn.main_service.types.IconProvider;
import pl.umk.mat.odyn.main_service.types.IconType;
import pl.umk.mat.odyn.main_service.types.IntentProvider;

/**
 Jest to klasa odpowiedzialna za tworzenie powiadomień.
 */
public class NotificationCreator {
	private Context context;
	public NotificationCreator(Context context) {
		this.context = context;
		emerg = getAction(IconType.emergency);
		recrd = getAction(IconType.recording);
		photo = getAction(IconType.photo);
		//back = getAction(IconType.back_to_app);
	}

	// użyj, aby utworzyć powiadomienie
	/**
	 Jest to metoda służąca do tworzenia powiadomień.
	 */
	private Notification.Action emerg;
	private Notification.Action recrd;
	private Notification.Action photo;
	private Notification.Action back;

	private static int code = 3;

	public Notification create() {
		Notification.Builder builder = new Notification.Builder(context);
		builder
				.setSmallIcon(R.drawable.cam_icon)
				//.setLargeIcon(bitmap) // TODO, potrzebna ikonka
				.setContentTitle(context.getString(R.string.notif_title))
				.setContentText(context.getString(R.string.notif_text))
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setVisibility(Notification.VISIBILITY_PUBLIC)
				.setAutoCancel(false)
				.setOngoing(false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(setChannel());
		}

		// ikonki akcji
		builder.addAction(emerg);
		builder.addAction(recrd);
		builder.addAction(photo);
		//builder.addAction(back);

		// zamknie się powiadomienie, to apka też się zamknie
		//builder.setDeleteIntent(pendingIntentProvider(IconType.close)); // i tak nie da się usunąć, bo podpięte do foreground service

		// naciśniesz > otworzy się apka. czy przyciski wliczone jako content?
		builder.setContentIntent(pendingIntentProvider(IconType.back_to_app));
		/*  ✔ utworzone
		 *  ✔ ikonka (domyślna)
		 *  ✔ tekst
		 *  ✔ akcja po naciśnięciu powiadomienia: otwórz aplikację
		 *  ✔ przyciski akcji: zdjęcie, nagraj, emergency, zamknij
		 *  ✔ nagrywanie
		 * */
		return builder.build();
	}

	// ustaw akcje dostępne pod przyciskami powiadomienia
	/**
	 Jest to metoda służąca do uzyskiwania akcji pod przyciskami powiadomienia.
	 @param iconType Typ ikony
	 */
	private Notification.Action getAction(IconType iconType) {
		//Log.d("NotificationCreator", ">>> Tworzę akcję dla " + iconType);
		Notification.Action.Builder builder = new Notification.Action.Builder(
				Icon.createWithResource(context, IconProvider.getIconId(iconType, true)),
				iconType.toString(),
				pendingIntentProvider(iconType)
		);
		// FIXME jeśli ikony nie wyświetlają się poprawnie, to zmień 4 linijki wyżej z getIcon(..., false) na getIcon(..., true)

		return builder.build();
	}

	/**
	 Jest to metoda służąca do tworzenia pending intentów, jest to intent nie wykonujący się od razu, tylko w chwili naciśnięcia przycisku w powiadomieniu.
	 @param type Typ ikony
	 */
	private PendingIntent pendingIntentProvider(IconType type) {
		//Log.d("NotificationCreator", ">>> Tworzę PendingIntenta dla " + type);
		Intent intent = IntentProvider.iconClicked(context, type);    // Tak, MainService wysyła do siebie te intenty futureTODO
		if(intent == null)
			Log.e("NotificationCreator", ">>> ERROR, intent nie istnieje, typ ikony: "+ type);
		return PendingIntent.getService(context, code++, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE); // Android 12 requires FLAG_MUTABLE or IMMUTABLE
	}

	/**
	 Jest to metoda służąca do tworzenia kanału powiadomień.
	 */
	@RequiresApi(api = Build.VERSION_CODES.O)
	private String setChannel() {
		String channelid = "CHANNEL1";
		NotificationChannel channel = new NotificationChannel(channelid, "Notification with buttons", NotificationManager.IMPORTANCE_DEFAULT);
		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(channel);
		return channelid;
	}

	/*
	public void closeNotification() {
		//
	}
	*/
}
