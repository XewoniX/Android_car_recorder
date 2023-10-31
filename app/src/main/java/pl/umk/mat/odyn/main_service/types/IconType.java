/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.main_service.types;

// typy ikon i powiązanych z nimi akcjami
/**
 * Jest to typ wyliczeniowy zawierający typy icon i powiązane z nimi akcje.
 */
public enum IconType {
	photo,
	recording,
	emergency,
	close,
	menu,
	back_to_app,

	// bez ikon, tylko akcje
	display_notif,
	hide_notif
}
