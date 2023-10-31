/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import pl.umk.mat.odyn.R;

/**
 * Jest to aktywność odpowiadająca za wyświetlanie listy nagrań.
 */
public class RecordingList extends AppCompatActivity {

    /**
     * Jest to metoda tworząca listę nagrań.
     * @param savedInstanceState Wiązka argumentów
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_list);
    }
}