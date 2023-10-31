/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.main_service.types;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pl.umk.mat.odyn.main_service.MainService;

// IntentProvider to klasa, która dla każdej ikony dostarcza (wykonuje) powiązaną z nią akcję
/**
 * Jest to klasa, która dla każdej ikony wykonuje powoiązaną z nią akcję.
 */
public class IntentProvider {

	public static final String extraName = "iconType";

	/**
	 * Jest to metoda, która dla każdej ikony dotarcza powiązaną z nią akcję.
	 * @param context Kontekst
	 * @param iconType Typ ikony
	 */
	public static Intent iconClicked(Context context, IconType iconType) {
		Class<?> actionReciever = MainService.class;
		Intent intent;
		switch(iconType) {
			case photo:
			case recording:
			case emergency:
			case back_to_app:
				//Log.d("IntentProvider", ">>> przekazano typ ikony " + iconType);
				intent = new Intent(context, actionReciever);
				intent.putExtra(extraName, iconType); // enum casted to Serializable
				return intent;
			case close:
				// close app
				//Log.d("IntentProvider", ">>> przekazano typ close");
				intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				return intent;
			default:	// default oraz menu
				Log.w("IntentProvider", ">>> intent niezdefiniowany dla "+ iconType);
				return null; // zabezpieczenie
		}
	}
}
