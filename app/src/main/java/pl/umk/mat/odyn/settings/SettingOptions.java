/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Damian Gałkowski <galdam@mat.umk.pl>, Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.settings;

/**
 * Jest to klasa zawierająca opcje w ustawieniach.
 */
public class SettingOptions {
	public static final String[] storageOptions = {"Karta SD", "Pamięć wewnętrzna"};
	public static final String[] LeftOrRight = {"Lewo", "Prawo"};
	public static final String[] LengthRecords = {"30s", "1 min", "2 min", "3 min", "4 min", "5 min"};
	public static final String[] SizeVideo = {"512MB", "1GB", "2GB", "4GB", "8GB", "12GB", "16GB", "32GB", "64GB", "128GB"};
	public static final String[] SizeEmergency = SizeVideo; // to to samo
	public static final String[] AccelerometerSensitivity = {"0.25 G", "0.5 G", "1 G", "1.5 G", "2 G", "2.5 G", "3 G"};
	public static final String[] MinimumSpeed = {"0 km/h", "10 km/h", "15 km/h", "20 km/h", "25 km/h", "30 km/h", "40 km/h", "50 km/h", "60 km/h"};

	// kolejność
	public static final String[][] optionsOrder = {null, storageOptions, LeftOrRight, LengthRecords, SizeVideo, SizeEmergency, AccelerometerSensitivity, MinimumSpeed}; // to tylko referencje

	// te nie będą w Layou'cie. Lista wartości wykorzystywana przy logice ustawień
	public static final int[] lengthValuesSeconds = {30, 60, 120, 180, 240, 300};  // musi odpowiadać wartościom w LengthRecords tylko że w sekundach
	public static final int[] sizeValuesMB = {512, 1024, 2048, 4096, 8192, 12288, 16384, 32768, 65536, 131072}; // musi odpowiadać wartościom w SizeVideo tylko że w MegaBajtach
	public static final double[] accelerometerSens = {0.25, 0.5, 1, 1.5, 2, 2.5, 3};
	public static final int[] minimumSpeed = {0, 10, 15, 20, 25, 30, 40, 50, 60};


	public static final boolean[] defaultSwitches = {false, true, true, true, true, true, true}; // indeksy domyślnych opcji, pierwszej nie ma
	public static final int[] defaultSpinners = {-1, 0, 0, 3, 3, 3, 3, 3}; // indeksy domyślnych opcji, pierwszej nie ma
}
