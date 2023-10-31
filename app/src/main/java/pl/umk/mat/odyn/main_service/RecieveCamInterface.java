/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.main_service;

import pl.umk.mat.odyn.cam.Cam;

/**
 Jest to interfejs odpowiedzialny za przekazywanie kamery z klasy Cam z MainScreen do MainActivity.
 */
public interface RecieveCamInterface {
	void recieveCam(Cam cam);
}
