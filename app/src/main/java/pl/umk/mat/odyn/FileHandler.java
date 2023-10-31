/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Damian Gałkowski <galdam@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn;

import pl.umk.mat.odyn.settings.SettingOptions;
import pl.umk.mat.odyn.settings.SettingsProvider;
import pl.umk.mat.odyn.settings.SettingNames;
import android.content.Context;
import android.util.Log;

import pl.umk.mat.odyn.cam.RecType;

import org.json.JSONException;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


/**
 * Jest to klasa odpowiedzialna za obsługę plików.
 */
public class FileHandler {
    // metoda, podajesz plik, typ i zapisuje pod odpowiednią ścieżką i nazwą
    private String dir;
    private String pictSubdir = "pictures";
    private String vidSubdir = "videos";
    private String emergSubdir = "emergency_recordings";
    private String dataSubdir = "data";
    private Context context;
    private SettingsProvider settingsProvider;
    private boolean isEmergency = false;


    public FileHandler(Context mainActivity) {
        // TODO ustawianie czy w pamięci telefonu, czy na karcie SD. (pobierane z ustawień)
        settingsProvider = new SettingsProvider();
        context = mainActivity;
        //dir = context.getFilesDir().getAbsolutePath();
        dir = context.getExternalMediaDirs()[0].getAbsolutePath();
        Log.d("FileHandler", ">>> Dir: " + dir);
        dir = removeSlash(dir) + '/' + "Odyn";
        Log.d("FileHandler", ">>> dir removSlash: " + dir);
        createDirIfNotExists(getDirPath(pictSubdir));
        createDirIfNotExists(getDirPath(vidSubdir));
        createDirIfNotExists(getDirPath(emergSubdir));
        createDirIfNotExists(getDirPath(dataSubdir));


        checkSize();
    }

    /**
     * Jest to metoda służąca do otrzymania ścieżki katalogu na podstawie podkatalogu.
     * @param subDir Podkatalog
     */
    private String getDirPath(String subDir) {
        return context.getExternalMediaDirs()[0].getAbsolutePath() + File.separator + "Odyn" + File.separator + subDir;
    }

    /**
     * Jest to metoda służąca do tworzenia katalogu jeżeli katalog o podanej ścieżce nie istnieje.
     * @param path Ścieżka do katalogu
     */
    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                Log.e("FileHandler", ">>> Nie udalo sie stworzyc katalogu: " + path);
            }
        }
    }

    // TODO przetestuj

    // TODO rozbuduj o zapis do katalogu w zależności od rodzaju pliku

    // TESTOWE:
    public String testPathGetExternal() {
        return context.getExternalMediaDirs()[0].getAbsolutePath(); // sdcard/Android/media/com.example.odyn/<tutaj pliki>
    }
    public String testMyDirPath() {
        return dir;
    }
    public String testPathExperiment() {
        return "XD";
    }



    /**
     * Jest to metoda służąca do otrzymywania limitu pamięci w MB.
     */
    private long getLimitFromSettings() {
        try {
            int selectedPosition = settingsProvider.getSettingInt(SettingNames.spinners[4]);
            long sizeInMB = SettingOptions.sizeValuesMB[selectedPosition];
            long sizeInBytes = sizeInMB * 1024 * 1024;
            Log.d("FileHandler", ">>> Aktualna wartość limitu filów: " + sizeInMB);
            return sizeInBytes;
        } catch (JSONException e) {
            Log.e("FileHandler", ">>> ERROR, "+ e);
            return 0;
        }
    }


    /**
     * Jest to metoda służąca do otrzymywania limitu pamięci z nagrań awaryjnych w MB.
     */
    private long getLimitFromEmergency() {
        try {
            int selectedPosition = settingsProvider.getSettingInt(SettingNames.spinners[5]);
            long sizeInMB = SettingOptions.sizeValuesMB[selectedPosition];
            long sizeInBytes = sizeInMB * 1024 * 1024;
            Log.d("FileHandler", ">>> Aktualna wartość limitu emergency: " + sizeInMB);
            return sizeInBytes;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Jest to metoda służąca do sprawdzania rozmiaru katalogu z plikami video.
     */
    public void checkSize() {
        long videoDirSize = getVideoDirSize(); // Pobierz sumaryczny rozmiar wideo
        long emergencyDirSize = getEmergencyDirSize(); // Pobierz sumaryczny rozmiar wideo awaryjnych
        long videoLimit = getLimitFromSettings(); // limit rozmiaru z ustawień
        long emergencyLimit = getLimitFromEmergency(); // limit rozmiaru z ustawień

        if (videoDirSize > videoLimit || emergencyDirSize > emergencyLimit) {
            int numVideosToDelete = 2; // Przykładowa liczba najstarszych nagran do usunięcia
            deleteOldestVideos(numVideosToDelete); // Usuń najstarsze nagrania z vidSubdir
            checkSize(); // Rekurencyjnie sprawdź rozmiar ponownie
        }
    }

    /**
     * Jest to metoda służąca do usuwania najstarszych filmów.
     * @param numVideosToDelete Ilość filmów do usunięcia
     */
    public void deleteOldestVideos(int numVideosToDelete) {
        File videoDir = new File(getDirPath(vidSubdir));
        File[] videoFiles = videoDir.listFiles();

        if (videoFiles != null && videoFiles.length > numVideosToDelete) {
            // Sortuj pliki w kolejności od najstarszego do najnowszego
            Arrays.sort(videoFiles, (file1, file2) -> {
                Long lastModified1 = file1.lastModified();
                Long lastModified2 = file2.lastModified();
                return lastModified1.compareTo(lastModified2);
            });

            // Usuń najstarsze pliki
            for (int i = 0; i < numVideosToDelete; i++) {
                if (videoFiles[i].delete()) {
                    Log.d("FileHandler", ">>> Usunięto plik: " + videoFiles[i].getName());
                } else {
                    Log.e("FileHandler", ">>> Nie udało się usunąć pliku: " + videoFiles[i].getName());
                }
            }
        }
    }

    /**
     * Jest to metoda służąca do przenoszenia nagrań awaryjnych z katalogu "videos" do katalogu "emergency_recordings".
     */
    public void moveEmergencyRecordings() {
        File videoDir = new File(getDirPath(vidSubdir));
        File[] videoFiles = videoDir.listFiles();

        if (videoFiles != null) {
            File emergDir = new File(getDirPath(emergSubdir));
            createDirIfNotExists(emergDir.getAbsolutePath()); // Upewnij się, że katalog "emergency_recordings" istnieje

            File latestFile = null;
            long latestTimestamp = 0;

            // Znajdź najnowszy plik
            for (File file : videoFiles) {
                if (file.isFile()) {
                    long timestamp = file.lastModified();
                    if (timestamp > latestTimestamp) {
                        latestFile = file;
                        latestTimestamp = timestamp;
                    }
                }
            }

            // Przenieś najnowszy plik do katalogu awaryjnego
            if (latestFile != null) {
                String fileName = latestFile.getName();
                String destinationPath = getDirPath(emergSubdir) + File.separator + fileName;

                File destinationFile = new File(destinationPath);
                if (latestFile.renameTo(destinationFile)) {
                    Log.d("FileHandler", ">>> Przeniesiono plik do awaryjnych: " + fileName);
                } else {
                    Log.e("FileHandler", ">>> Nie udało się przenieść pliku do awaryjnych: " + fileName);
                }
            }
        }
    }
    /**
     * Jest to metoda służąca do otrzymywania rozmiaru katalogu.
     * @param directoryPath Ścieżka do katalogu
     */
    private long getDirectorySize(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        long size = 0;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDirectorySize(file.getAbsolutePath());
                }
            }
        }
        return size;
    }
    /**
     * Jest to metoda służąca do otrzymywania rozmiaru katalogu z plikami obrazów.
     */
    public long getPictureDirSize() {
        String pictureDirPath = getDirPath(pictSubdir);
        return getDirectorySize(pictureDirPath);
    }

    /**
     * Jest to metoda służąca do otrzymywania rozmiaru katalogu z plikami video.
     */
    public long getVideoDirSize() {
        String videoDirPath = getDirPath(vidSubdir);
        return getDirectorySize(videoDirPath);
    }

    /**
     * Jest to metoda służąca do otrzymywania rozmiaru katalogu z plikami video nagrywanymi w trybie awaryjnym.
     */
    public long getEmergencyDirSize() {
        String emergencyDirPath = getDirPath(emergSubdir);
        return getDirectorySize(emergencyDirPath);
    }

    /**
     * Jest to metoda służąca do otrzymywania rozmiaru katalogu z plikami video i obrazami.
     */
    public long getTotalDirSize() {
        return getPictureDirSize() + getVideoDirSize() + getEmergencyDirSize();
    }

    // TWORZENIE PLIKÓW
    /**
     * Jest to metoda służąca do tworzenia plików.
     * @param namePrefix Prefix pliku
     * @param format Format pliku
     */
    public File createFile(String namePrefix, String format) {
        String fileName = youNameIt(namePrefix, format);
        File file = new File(context.getExternalMediaDirs()[0].getAbsolutePath(), fileName);
        return file;
    }

    @Deprecated
    public File createFile(RecType type) { // bez sensu, nie korzystać
        switch (type) {
            case picture:
                return createPicture();
            case video:
                return createVideo("mp4");
            case emergency:
                return createEmergencyVideo("mp4"); // tymczasowo
            case data:
                return createDataFile("txt"); // nie wiem jaki format
        }
        return null;
    }

    /**
     * Jest to metoda służąca do tworzenia plików obrazów w formacie ODYN-img-yyyy-MM-dd_HH-mm-ss.jpg.
     */
    public File createPicture() {
        String fileName = youNameIt("ODYN-img", "jpg");
        File file = new File(getDirPath(pictSubdir), fileName);
        // getExternalMediaDirs()[0] = wylistuj mi zewnętrzne nośniki danych i wybierz pierwszy
        // TODO wybierana ścieżka zapisu
        return file;
    }

    /**
     * Jest to metoda służąca do tworzenia plików video w formacie ODYN-vid-yyyy-MM-dd_HH-mm-ss.mp4.
     * @param format Format pliku
     */
    public File createVideo(String format) {
        String fileName = youNameIt("ODYN-vid", format);
        File file = new File(getDirPath(vidSubdir), fileName);
        Log.d("FileHandler", ">>> Ścieżka pliku: " + file.getAbsolutePath());
        // Sprawdź rozmiar katalogu wideo
        long videoDirSize = getVideoDirSize();
        Log.d("FileHandler", ">>> Rozmiar katalogu wideo: " + videoDirSize);

        return file;
    }

    /**
     * Jest to metoda służąca do tworzenia plików video nagrywanych w tle w formacie ODYN-emr-yyyy-MM-dd_HH-mm-ss.mp4.
     * @param format Format pliku
     */
    public File createEmergencyVideo(String format) {
        String fileName = youNameIt("ODYN-emr", format);
        File file = new File(getDirPath(emergSubdir), fileName);
        return file;
    }

    /**
     * Jest to metoda służąca do tworzenia plików związanych z danymi w formacie ODYN-dat-yyyy-MM-dd_HH-mm-ss.srt.
     */
    public File createDataFile(String format) {
        String fileName = youNameIt("ODYN-dat", format);
        File file = new File(getDirPath(dataSubdir), fileName);
        return file;
    }

    /**
     * Jest to metoda służąca do nazywania plików wraz z podanym przez użytkownika formatem pliku.
     * @param namePrefix Prefix pliku
     * @param fileFormat Format pliku
     */
    private String youNameIt(String namePrefix, String fileFormat) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        return namePrefix + '-' + timeStamp + '.' + fileFormat;
    }

    /**
     * Jest to metoda służąca do tworzenia katalogu.
     * @param path Ścieżka do katalogu
     */
    private void createDir(String path) {
        if(!ifDirExists(path)) {
            new File(path).mkdir();
        }
    }

    /**
     * Jest to metoda służąca do sprawdzenia czy dany katalog istnieje.
     * @param path Ścieżka do katalogu
     */
    private boolean ifDirExists(String path) {
        return new File(path).exists();
    }

    // usuwa '/' jeśli jest na ostatniej pozycji
    /**
     * Jest to metoda służąca do usuwania "/" ze ścieżki jeżeli znajduje się na ostatniej pozycji.
     * @param path Ścieżka do katalogu
     */
    private String removeSlash(String path) {
        int lastPos = path.length()-1;
        if(path.charAt(lastPos) == '/') {
            path = path.substring(0,lastPos); // path bez ostatniego znaku '/' jeśli był
        }
        return path;
    }
}



























    /*
    public void createFile(File absFile, Type type) {
        // argument absFile - plik bez podanej ścieżki (abstract pathname)
        File file = createFile(type);
        file = absFile.
    }
    */
    /*
    private File createFileTemplate(File dirPath) {
        //
        return null;
    }
    */

    /*
    private boolean fileExists(URI uri) {
        //
    }
     */
    /* // NIE POTRZEBA CZYTAĆ PLIKÓW
    public File readFile(URI uri) {
        //
        File plik = new File(uri);
        if()
    }
    public File readFile(String path, String filename) throws URISyntaxException {
        path = removeSlash(path);
        path += '/' + filename;
        return readFile(new URI(path));
    }
     */

