/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Damian Gałkowski <galdam@mat.umk.pl>, Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import pl.umk.mat.odyn.R;
import pl.umk.mat.odyn.settings.SettingNames;
import pl.umk.mat.odyn.settings.SettingOptions;
import pl.umk.mat.odyn.settings.SettingsProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;

/**
 * Jest to aktywność odpowiadająca za obsługę panelu aplikacji.
 */
public class Settings extends AppCompatActivity {
    private SwitchMaterial switch1;
    private SwitchMaterial switch2;
    private SwitchMaterial switch3;
    private SwitchMaterial switch4;
    private SwitchMaterial switch5;
    private SwitchMaterial switch6;
    //private int mode;
    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;
    private Spinner spinner4;
    private Spinner spinner5;
    private Spinner spinner6;
    private Spinner spinner7;

    // elementy umieściłem w tablicy, dla ułatwienia użytkowania
    private SwitchMaterial[] mSwitch;
    private Spinner[] spinners;

    /**
     * Jest to metoda tworząca widok ustawień aplikacji.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SettingsProvider settingsProvider = new SettingsProvider();
        settingsProvider.loadSettings(this);

        findUIElements();
        setSwitches();
        initSpinners();
        setSwitchListeners();
        setSpinnerListeners();

        loadSettingsFromFile();
    }


    /**
     * Jest to metoda odpowiedzialna za znajdowanie elementów UI w XML.
     */
    private void findUIElements() {
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);
        switch4 = findViewById(R.id.switch4);
        switch5 = findViewById(R.id.switch5);
        switch6 = findViewById(R.id.switch6);
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        spinner4 = findViewById(R.id.spinner4);
        spinner5 = findViewById(R.id.spinner5);
        spinner6 = findViewById(R.id.spinner6);
        spinner7 = findViewById(R.id.spinner7);
        mSwitch = new SwitchMaterial[] {null, switch1, switch2, switch3, switch4, switch5, switch6};
        spinners = new Spinner[] {null, spinner1, spinner2, spinner3, spinner4, spinner5, spinner6, spinner7};
    }

    /**
     * Jest to metoda odpowiedzialna za ustawienie wartości przełączników.
     */
    private void setSwitches() {
        // Ustawienie wartości przełączników
        String[] sw = SettingNames.switches;
        SettingsProvider sprov = new SettingsProvider();
        for(int i = 1; i < mSwitch.length; i++) {
            try {
                mSwitch[i].setChecked(sprov.getSettingBool(sw[i]));
            } catch (JSONException e) {
                Log.e("Settings", ">>> nie odczytano stanu ustawień");
            }
        }
    }

    /**
     * Jest to metoda odpowiedzialna za ustawienie opcji spinnerów.
     */
    private void initSpinners() {
        // Ustawienie opcji spinnera
        //int spinnerTypeCode = android.R.layout.simple_spinner_dropdown_item;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.storageOptions);
        spinner1.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.LeftOrRight);
        spinner2.setAdapter(adapter2);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.LengthRecords);
        spinner3.setAdapter(adapter3);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.SizeVideo);
        spinner4.setAdapter(adapter4);

        ArrayAdapter<String> adapter5 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.SizeEmergency);
        spinner5.setAdapter(adapter5);

        ArrayAdapter<String> adapter6 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.AccelerometerSensitivity);
        spinner6.setAdapter(adapter6);

        ArrayAdapter<String> adapter7 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SettingOptions.MinimumSpeed);
        spinner7.setAdapter(adapter7);
    }

    /**
     * Jest to metoda odpowiedzialna za dodanie obsługi zdarzeń dla przełączników.
     */
    private void setSwitchListeners() {
        // Dodanie obsługi zdarzeń dla przełączników
        for(int i = 1; i < mSwitch.length; i++) {
            mSwitch[i].setOnCheckedChangeListener(this::switchListener);
        }
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                new SettingsProvider().setSetting(SettingNames.switches[1], isChecked);
            } catch (JSONException e) {
                Log.w("Settings", ">>> nie udało się zapisać ustawienia");
            }

            //AppCompatDelegate.setDefaultNightMode(mode);
            // saveSettingsToFile(); // zbędne, nie potrzeba co chwilę nadpisywać pliku, ustawienia w aplikacji są dostępne przez SettingsProvider.
            //  zapisanie do pliku po wyjściu z tej aktywności
        });
    }

    /**
     * Jest to metoda odpowiedzialna za dodanie obsługi zdarzeń dla spinnerów.
     */
    private void setSpinnerListeners() {
        for (int i = 1; i < spinners.length; i++) {
            spinners[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    saveSettingsToFile();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Nie trzeba implementować, jeśli nie jest potrzebne
                }
            });
        }
    }


    /**
     * Jest to metoda odpowiedzialna za dodanie obsługi zdarzeń dla konkretnego przełącznika.
     */
    private void switchListener(CompoundButton buttonView, boolean isChecked) {
        for(int i = 1; i < mSwitch.length; i++) {
            if(buttonView == mSwitch[i]) {
                try {
                    new SettingsProvider().setSetting(SettingNames.switches[i], isChecked);
                } catch (JSONException e) {
                    Log.w("Settings", ">>> nie udało się zapisać ustawienia");
                }
                //saveSettingsToFile(); // ??? wydaje się niepotrzebne

                break; // jak już znaleźliśmy element, to nie iterujemy dalej
            }
        }
    }


    /**
     * Jest to metoda wywoływana po zatrzymaniu aplikacji.
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveSettingsToFile();
    }

    /**
     * Jest to metoda zapisująca ustawienia do pliku.
     */
    private void saveSettingsToFile() {
        // w zasadzie, to teżź zbędne, opcje zapisują się przy przełączaniu
        try {
            SettingsProvider sprov = new SettingsProvider();

            for(int i = 1; i < mSwitch.length; i++)
                sprov.setSetting(SettingNames.switches[i], mSwitch[i].isChecked());
            for (int i = 1; i < spinners.length; i++)
                sprov.setSetting(SettingNames.spinners[i], spinners[i].getSelectedItemPosition());


            //settings.put("mode", mode); // po co dodatkowo zapisujemy mode, skoro jest też w przełączniku?

            new SettingsProvider().writeSettings(this); // zapisz ustawienia
        } catch (JSONException e) {
            Log.e("Settings", ">>> błąd podczas tworzenia obiektu JSON");
            e.printStackTrace();
        }
    }


    /**
     * Jest to metoda uruchamiająca ustawienia z pliku.
     */
    private void loadSettingsFromFile() {
        try {
            SettingsProvider sprov = new SettingsProvider();

            // ustawienie opcji
            for(int i = 1; i < mSwitch.length; i++)
                mSwitch[i].setChecked(sprov.getSettingBool(SettingNames.switches[i]));
            for (int i = 1; i < spinners.length; i++)
                spinners[i].setSelection(sprov.getSettingInt(SettingNames.spinners[i]));


            //mode = sprov.getSettingInt("mode");
            //AppCompatDelegate.setDefaultNightMode(mode);

        } catch (JSONException e) {
            Log.e("Settings", ">>> błąd podczas odczytu ustawień");
            e.printStackTrace();
        }
    }

    /**
     * Jest to metoda wywoływana przez Android, gdy aktywność jest niszczona.
     * Zamyka wątki, Usuwa referencje na siebie.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new SettingsProvider().writeSettings(this);
    }
}