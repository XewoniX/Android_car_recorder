/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Damian Gałkowski <galdam@mat.umk.pl>, Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.settings;

/**
 * Jest to klasa zawierająca nazwy ustawień.
 */
public class SettingNames {
	public static final String[] switches = {
			"no 0", "AI", "dispLoc", "saveLoc", "dispSpeed", "saveSpeed", "recAudio"
	};
	public static final String[] spinners = {
			"no 0", "storage_option", "Left_Right", "Length_record", "Size_video", "Size_emergency", "Accelerometer_sensitivity", "Min_speed"
	};
}
	/*
	public static Map<String, Integer> settingIndex = createMap();
	private static Map<String, Integer> createMap() {
		//
		Map<String, Integer> map = new HashMap<>(switches.length - 1 + spinners.length - 1); // odejmuję po 1, bo nie będzie pierwszego elementu
		for(int i = 1; i < switches.length; i++) {
			map.put(switches[i], i);
		}
		for(int i = 1; i < spinners.length; i++) {
			map.put(spinners[i], i);
		}
		return null;
	}
	public static int getSettingIndex(String settingName) {
		Integer index = settingIndex.get(settingName);
		if(index == null) {
			return 0;
		}
		return index;
	}
	*/

