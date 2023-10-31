/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.cam;

/**
 * Jest to interfejs odpowiedzialny za otrzymywanie informacji z kamery.
 */
public interface GetCamInterface {
	void getCamInfoLater(CamInfo camInfo);
}
