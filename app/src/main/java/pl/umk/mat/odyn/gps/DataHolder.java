/*
    BSD 3-Clause License
    Copyright (c) Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.gps;

/**
 * Klasa odpowiedzialna za przechowywanie danych związanych z GPS, aby można było uzyskać do nich dostęp z innych klas
 * Class responsible for holding GPS-related data so it can be accessed by multiple other classes
 */
public class DataHolder {
	private static DataHolder instance;

	private String longitude;
	private String latitude;
	private String speed;
	private String counter;
	private String timer;

	/**
	 * Constructor
	 */
	private DataHolder() {	}

	/**
	 * Metoda odpowiedzialna za zwrócenie instancji DataHolder, jeśli istnieje. Jeśli nie, tworzy jedną.
	 * Method responsible for returning instance of DataHolder, if it exists. If it doesn't, creates one.
	 * @return instance
	 */
	public static synchronized DataHolder getInstance() {
		if (instance == null) {
			instance = new DataHolder();
		}
		return instance;
	}

	/**
	 * Metoda zwracająca zapisaną długość geograficzną
	 * Method returning stored longitude
	 * @return longitude
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * Metoda używana do aktualizacji wartości długości geograficznej
	 * Method used to update longitude value
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	/**
	 * Metoda zwracająca zapisaną szerokość geograficzną
	 * Method returning stored latitude
	 * @return latitude
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * Metoda używana do aktualizacji wartości szerokości geograficznej
	 * Method used to update latitude value
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * Metoda zwracająca zapisaną prędkość
	 * Method returning stored speed
	 * @return speed
	 */
	public String getSpeed() {
		return speed;
	}

	/**
	 * Metoda używana do aktualizacji wartości prędkości
	 * Method used to update speed value
	 */
	public void setSpeed(String speed) {
		this.speed = speed;
	}

	/**
	 * Metoda zwracająca zapisany licznik
	 * Method returning stored counter
	 * @return counter
	 */
	public String getCounter() {
		return counter;
	}

	/**
	 * Metoda używana do aktualizacji wartości licznika
	 * Method used to update counter value
	 */
	public void setCounter(String counter) {
		this.counter = counter;
	}

	/**
	 * Metoda zwracająca zapisany timer
	 * Method returning stored timer
	 * @return counter
	 */
	public String getTimer() {
		return timer;
	}

	/**
	 * Metoda używana do aktualizacji wartości timera
	 * Method used to update timer value
	 */
	public void setTimer(String timer) {
		this.timer = timer;
	}
}
