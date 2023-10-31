/*
    BSD 3-Clause License
    Copyright (c) Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.gps;

import android.annotation.SuppressLint;
import android.util.Log;
import pl.umk.mat.odyn.settings.SettingNames;
import pl.umk.mat.odyn.settings.SettingsProvider;
import org.json.JSONException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Wątek odpowiedzialny za zapisywanie informacji GPS do pliku .srt
 * Thread responsible for writing GPS info into .srt file
 */
public class SRTWriter extends Thread {

    private boolean stopWriting = false;
    private File file;
	private String srtLine;
	private int prevLineCountEnd = 0;

	public SRTWriter(File file) {
		this.file = file;
	}

    @Override
    public void run() {
        try {
            FileWriter writer = new FileWriter(file, true); // true for appending
            while (!stopWriting) {
				DataHolder dataHolder = DataHolder.getInstance();
				String timerText = dataHolder.getTimer();
				String latitudeText = dataHolder.getLatitude();
				String longitudeText = dataHolder.getLongitude();
				Boolean writeLocation, writeSpeed;
				try {
					SettingsProvider settingsProvider = new SettingsProvider();
					writeLocation = settingsProvider.getSettingBool(SettingNames.switches[3]);
					writeSpeed = settingsProvider.getSettingBool(SettingNames.switches[5]);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
				if (timerText != null && latitudeText != null && longitudeText != null) {
                    // Write the data in SRT format
					int counterText = Integer.parseInt(dataHolder.getCounter());
					srtLine = "\n";
					if (prevLineCountEnd != 0 && counterText - 1 != prevLineCountEnd) {
						counterText = prevLineCountEnd;
					}
					//Log.d("SRTWriter", "Counter value: " + dataHolder.getCounter());
					srtLine += secondsToTimestamp(counterText - 1) + " --> " + secondsToTimestamp(counterText);
					srtLine += "\n" + timerText;
					if (writeSpeed) {
						srtLine += " | " + dataHolder.getSpeed();
					}
					if (writeLocation) {
						srtLine += " | " + latitudeText + ", ";
						srtLine += longitudeText;
					}
					srtLine += "\n\n";
					//Log.d("GPS", srtLine); // turned off to prevent spamming Logs
					writer.write(srtLine);
					writer.flush();
					//Log.d("GPSThread","Wrote srt line");
                }

                // Sleep for a while before checking again
                Thread.sleep(1000);
            }
            writer.close();
        } catch (IOException | InterruptedException e) {
			e.printStackTrace();
			Log.d("GPS","Exception/End of srt writing");
        }
    }

	/**
	 * Metoda zakończenia zapisu do pliku .srt
	 * Method to finish writing into .srt file
	 */
    public void stopWriting() {
        stopWriting = true;
		interrupt();
    }

	/**
	 * Metoda konwersji aktualnej długości nagrania z sekund na hh:mm:ss:ms
	 * Method to convert current length of recording from seconds to hh:mm:ss:ms
	 */
	@SuppressLint("DefaultLocale")
	public static String secondsToTimestamp(int seconds) {
		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		int secs = seconds % 60;
		int millis = 0; // set milliseconds to 0
		return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
	}

}

