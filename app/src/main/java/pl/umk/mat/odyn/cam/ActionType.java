/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.cam;

/*
	RecType to rodzaj nagrania/zdjęcia z kamery.

	ActionType to co należy zrobić z kamerą.

*/

/**
 * Jest to typ wyliczeniowy odpowiadający za działania związane z kamerą.
 */
public enum ActionType {
	stop,	// wyłącz nagrywanie
	start,	// włącz nagrywanie albo zrób zdjęcie
	toggle	// przełącz nagrywa/nienagrywa
}
