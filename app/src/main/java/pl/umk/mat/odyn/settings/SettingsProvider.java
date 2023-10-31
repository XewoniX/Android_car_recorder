/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Damian Gałkowski <galdam@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Jest to klasa odpowiedzialna za pobieranie informacji o ustawieniach w dowolnym miejscu aplikacji.
 */
public class SettingsProvider {
	private static JSONObject settings;

	// podaj nazwę ustawienia, zwróci wartość
	/**
	 * Jest to metoda zwracająca wartość ustawienia na podstawie nazwy.
	 * @param settingName Nazwa ustawienia
	 */
	public synchronized boolean getSettingBool(String settingName) throws JSONException {
		return settings.getBoolean(settingName);
	}

	/**
	 * Jest to metoda zwracająca wartość ustawienia na podstawie nazwy.
	 * @param settingName Nazwa ustawienia
	 */
	public synchronized int getSettingInt(String settingName) throws JSONException {
		return settings.getInt(settingName);
	}

	/**
	 * Jest to metoda odpowiedzialna za zapisywanie/nadpisywanie zmienionego ustawienia.
	 * @param settingName Nazwa ustawienia
	 * @param value Wartość
	 */
	// jeżeli tylko jedna rzecz się zmieniła. z założenia pisać będzie tylko Settings.java
	public synchronized void setSetting(String settingName, boolean value) throws JSONException {
		settings.put(settingName, value);
	}

	/**
	 * Jest to metoda odpowiedzialna za zapisywanie/nadpisywanie zmienionego ustawienia.
	 * @param settingName Nazwa ustawienia
	 * @param value Wartość
	 */
	public synchronized void setSetting(String settingName, int value) throws JSONException {
		settings.put(settingName, value);
	}

	/**
	 * Jest to metoda zapisująca/nadpisująca plik ustawień.
	 * @param context Kontekst
	 * @param settings Ustawienia
	 */
	// zapisuje / nadpisuje plik ustawień. z założenia pisać będzie tylko Settings.java
	private synchronized void writeSettings(Context context, JSONObject settings) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput("settings.json", MODE_PRIVATE)));
			writer.write(settings.toString());
			writer.close();
		} catch (IOException e) {
			Log.e("SettingsProvider", ">>> Nie udało się zapisać ustawień");
		}
	}

	/**
	 * Jest to metoda zapisująca/nadpisująca plik ustawień.
	 * @param context Kontekst
	 */
	public void writeSettings(Context context) {
		writeSettings(context, settings);
	}

	/**
	 * Jest to metoda zapisująca początkowe wartości w ustawieniach przy pierwszym uruchomieniu aplikacji.
	 */
	private void firstWriteSetting() {
		try {
			settings = new JSONObject();
			for (int i = 1; i < SettingNames.switches.length; i++)
				settings.put(SettingNames.switches[i], SettingOptions.defaultSwitches[i]);
			for (int i = 1; i < SettingNames.spinners.length; i++)
				settings.put(SettingNames.spinners[i], SettingOptions.defaultSpinners[i]);
		} catch (JSONException e) {
			Log.e("SettingsProvider", ">>> Nie udało się wqpisać początkowych wartości");
		}

	}

	// odczytuje ustawienia z pliku. wywołać na początku działania Aplikacji
	/**
	 * Jest to metoda odczytująca ustawienia z pliku, wywoływana na początku działania aplikacji.
	 * @param context Kontekst
	 */
	public synchronized void loadSettings(Context context) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput("settings.json")));
			StringBuilder stringBuilder = new StringBuilder();
			while (reader.ready()) {
				stringBuilder.append(reader.readLine()).append('\n');
			}
			reader.close();
			settings = new JSONObject(stringBuilder.toString());

			// akcje związane z ustawieniami, np. włącz ciemny motyw.
			//settingActions();
		} catch(FileNotFoundException e) {
			firstWriteSetting();
			return;
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		if (settings == null) {
			firstWriteSetting();
		}
	}
}
	/*// akcje związane z ustawieniami, np. włącz ciemny motyw. Jak będzie dużo akcji to przenieść do nowej klasy
	private void settingActions() {
		try {
			boolean isChecked = this.getSettingBool("mode");
			int mode;
			if (isChecked) {
				mode = AppCompatDelegate.MODE_NIGHT_YES;
			} else {
				mode = AppCompatDelegate.MODE_NIGHT_NO;
			}
			AppCompatDelegate.setDefaultNightMode(mode);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

*/