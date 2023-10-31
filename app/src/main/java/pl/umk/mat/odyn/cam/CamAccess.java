/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, Jakub Orłowski <orljak@mat.umk.pl>, Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.cam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import pl.umk.mat.odyn.FileHandler;
import pl.umk.mat.odyn.R;
import pl.umk.mat.odyn.gps.DataHolder;
import pl.umk.mat.odyn.gps.SRTWriter;
import pl.umk.mat.odyn.settings.SettingNames;
import pl.umk.mat.odyn.settings.SettingOptions;
import pl.umk.mat.odyn.settings.SettingsProvider;
import pl.umk.mat.odyn.main_service.types.ServCounter;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Jest to klasa odpowiadająca za dostęp do kamery.
 */
@SuppressWarnings("ALL")
public class CamAccess {
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private SettingsProvider settingsProvider;
    private GetCamInterface doItLaterIntf = null; // jeśli trzeba zaczekać na otrzymanie CamInfo()
    protected Activity main; // póki co spełnia dwie role: wątek (Context) i aktywność (wyświetlanie), później warto rozważyć rozdzielenie
    private boolean isEmergency = true;
    // korzysta z tego też klasa Cam (dziedziczy)
    /* Activity używane do:
        dostarczenia FileHandler'owi kontekstu x2
        bind widoku PreviewView
        otrzymanie CameraProvider
        otrzymanie wykonawcy x3
        menedżera okien
        otrzymanie LiveCycleOwner
        otrzymanie CameraManager
        utworzenie tosta
    */
    // konstruktor. PreviewView służy do wyświetlenia w nim obrazu z kamery
    public CamAccess(Activity main) {
        this.main = main;
        settingsProvider = new SettingsProvider();
        PreviewView prView2 = main.findViewById(R.id.previewView);
        cameraProviderSetup(prView2);
        Log.v("CamAccess", ">>> CamAccess constructor");

        ServCounter.camStarted();
        //
        if(doItLaterIntf != null) {
            doItLaterIntf.getCamInfoLater(getCamInfo());
        }
        setEmergencyFlag(true);

        handleEmergencyRecording();
    }
    private void setEmergencyFlag(boolean isEmergency) {
        this.isEmergency = isEmergency;
    }

    private void handleEmergencyRecording() {
        Log.d("CamAccess", "handleEmergencyRecording()");
        Log.d("CamAccess", "isEmergency: " + isEmergency);

        if (isEmergency) {
            FileHandler fileHandler = new FileHandler(main);
            fileHandler.moveEmergencyRecordings();
        }
    }

    public void setEmergencyMode(boolean emergencyMode) {
        setEmergencyFlag(emergencyMode);
        handleEmergencyRecording();

        if (!emergencyMode) {
            isEmergency = true;
        }
    }

    private long getLimitLength() {
        try {
            int selectedPosition = settingsProvider.getSettingInt(SettingNames.spinners[3]);
            long limit = SettingOptions.lengthValuesSeconds[selectedPosition];
            //Log.d("CamAccess", ">> Aktualna wartość limitu: " + limit);
            return limit;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // te dwie poniższe funkcje służą do przygotowania kamery do przekazywania obrazu do <PreviewView> i robienia zdjęć
    /**
     * Jest to metoda służąca do przygotowania kamery do przekazywania obrazu.
     * @param prView Widok podglądu
     */
    @SuppressLint("RestrictedApi")
    private void cameraProviderSetup(PreviewView prView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(main);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, prView);
            } catch (ExecutionException | InterruptedException e) {
                // gdzie przechwycenie ???
            }
        }, ContextCompat.getMainExecutor(main));
    }

    /**
     * Jest to metoda służąca do obsługi robienia zdjęć.
     * @param cameraProvider Dostawca kamery
     * @param prView Widok podglądu
     */
    @SuppressLint("RestrictedApi")
    private void bindPreview(ProcessCameraProvider cameraProvider, PreviewView prView) {

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up the preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(prView.getSurfaceProvider());
        ImageCapture.Builder builder = new ImageCapture.Builder();

        imageCapture = builder
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(main.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        /* TODO settings nagrywanie z i bez dzwieku */
        //Teoretycznie switch działa, ale nie działa wyłącznie audio w nagraniu, testowałem na sucho ustawienia
        //które dają wyciszenie audio a i tak dalej u mnie było
        boolean switchValue;
        try {
            switchValue = settingsProvider.getSettingBool(SettingNames.switches[6]);
            Log.d("CamAccess", ">>> Wartość przełącznika: " + switchValue);
        } catch (JSONException e) {
            Log.w("CamAccess", ">>> nie udało się odczytać wartości przełącznika 6\n" + e);
            switchValue = SettingOptions.defaultSwitches[6];
        }
        if (switchValue) {
            VideoCapture.Builder builder_vid = new VideoCapture.Builder();
            videoCapture = builder_vid
                    .setVideoFrameRate(60)
                    .setAudioChannelCount(1)
                    .setAudioBitRate(64000)
                    .build();
        } else {
            VideoCapture.Builder builder_vid_noaudio = new VideoCapture.Builder();
            videoCapture = builder_vid_noaudio
                    .setVideoFrameRate(60)
                    .setAudioChannelCount(0)
                    .setAudioBitRate(64000)
                    .build();
        }

        // użyj kamery do wyświetlania w mainActivity (preview) i do robienia zdjęć (imageCapture)
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) main, cameraSelector, preview, imageCapture, videoCapture);
    takeVideo(true);
    }
    // robi zdjęcie
    /**
     * Jest to metoda odpowiadająca za funkcję obsługi robienia zdjęcia.
     * @param file Plik
     */
    public void takePicture(File file) {
        // Set up the output file and capture the image
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(main), new ImageCapture.OnImageSavedCallback() {

            /**
             * Jest to metoda służąca do informacji o zapisie zdjęcia.
             * @param outputFileResults Wyniki pliku wyjściowego
             */
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // The image has been saved to the file
                Log.v("CamAccess", "---------ZapisywanieIMG---------");
            }

            /**
             * Jest to metoda służąca do informacji o wystąpionym błędzie przy zapisie zdjęcia.
             * @param exception Wyjątek
             */
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle any errors here
            }
        });
    }

    /**
     * Jest to klasa dostarczająca informacje o kamerze.
     */
    public class CamInfoProvider {
        private float FOV = 0;
        private float width = 0, height = 0;
        private Bitmap BMP = null;

        /**
         * Jest to metoda odpowiedzialna za konwersję obrazu na bitmappę.
         * @param imageProxy Obraz zamieniany na bitmapę
         */
        private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
            Image image = imageProxy.getImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        /**
         * Jest to metoda odpowiedzialna za dostarczanie obrazu do analizy AI.
         */
        private void takePictureBMP(){
            imageCapture.takePicture(ContextCompat.getMainExecutor(main), new ImageCapture.OnImageCapturedCallback() {
                /**
                 * Jest to metoda odpowiedzialna za obsługę otrzymania bitmapy.
                 * @param image Obraz
                 */
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    // Tutaj otrzymujesz obraz z kamery, możesz go przetwarzać lub zapisać w pamięci
                    // Uwaga: Ta metoda jest wywoływana na innym wątku, więc musisz obsłużyć go odpowiednio
                    super.onCaptureSuccess(image);
                    BMP = imageProxyToBitmap(image);
                    // Zapisz obraz w pamięci podręcznej
                }

                /**
                 * Jest to metoda odpowiedzialna za obsługę błędu przy tworzeniu bitmapy.
                 * @param exception Wyjątek
                 */
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    // Obsłuż błędy związane z wykonywaniem zdjęcia
                    super.onError(exception);
                }
            });
        }

        /**
         * Jest to metoda obliczająca pole widzenia.
         * @param focalLength Długość ogniskowa
         * @param aperture Otwór
         */
        private float calculateFOV(float focalLength, float aperture) {
            float horizontalFOV = (float) (2 * Math.atan2(aperture, (2 * focalLength)));
            float verticalFOV = (float) (2 * Math.atan2(aperture, (2 * focalLength)));
            return (float) Math.toDegrees(Math.sqrt(Math.pow(horizontalFOV, 2) + Math.pow(verticalFOV, 2)));
        }

        /**
         * Jest to metoda ustawiająca pole widzenia, szerokość i wysokość.
         */
        private void getInfo() { // ustawia FOV, width i height
            try {
                CameraManager cameraManager = (CameraManager) main.getSystemService(Context.CAMERA_SERVICE);
                String cameraId = cameraManager.getCameraIdList()[1]; // wybierz pierwszą kamerę
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                // uzyskanie wartości FOV
                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                float[] apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                FOV = calculateFOV(focalLengths[0], apertures[0]);

                // uzyskanie wartości rozdzielczości
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                Size resolution = sizes[0];
                width = resolution.getWidth();
                height = resolution.getHeight();

            } catch (CameraAccessException e) {
                e.printStackTrace();
                Toast.makeText(main, "Wystąpił błąd podczas korzystania z kamery", Toast.LENGTH_LONG).show();
                Log.e("CamAccess", ">>> Wystąpił błąd podczas korzystania z kamery");
            }
        }

        /**
         * Jest to metoda służąca do otrzymywania informacji takich jak:
         * obiekt przechowujący bitmapę, FOV,szerokość i wysokość obrazu.
         */
        public CamInfo getCamInfo() {
            if(FOV == 0 || width == 0 || height == 0) {
                getInfo();
            }
            takePictureBMP();
            return new CamInfo(FOV, width, height, BMP);
        }
    }

    /**
     * Jest to metoda służąca do otrzymywania informacji takich jak:
     * obiekt przechowujący bitmapę, FOV,szerokość i wysokość obrazu.
     * Przed wywołaniem getCamInfo() należy sprawdzić za pomocą canIgetCamInfo(), czy można to wywołać,
     * ponieważ składowa imageCapture, z której te metody korzystają, może jeszcze nie być zainicjalizowana.
     *
     * Problem z wywołaniem tej metody może nastąpić na początku programu, zaraz po konstruktorze Cam albo CamAccess.
     * Jeśli nie da się uruchomić, to można poczekać chwilę, albo skorzystać z setImgCaptureCreatedListener().
     *
     * // Sprawdzenie:
     * if(cam.canIgetCamInfo()) {
     *     cam.getCamInfo();
     * }
     *
     */
    public CamInfo getCamInfo() {
        // może się zdarzyć, że imageCapture (getCamInfo() z tego korzysta) nie jest jeszcze zainicjalizowane.
        if(imageCapture != null) {
            return new CamInfoProvider().getCamInfo();
        }
        return null;
    }

    /**
     * Jest to metoda służąca do sprawdzenia, czy można wykonać getCamInfo()
     */
    public boolean canIgetCamInfo() {
        return imageCapture != null;
    }
    /**
     * Jeśli nie można jeszcze wykonać getCamInfo() można tu podać dowolną metodę zwraca (void, przyjmuje CamInfo),
     * która ma się wykonać, po tym jak będzie już możliwe wykonanie getCamInfo().
     * @param interf Interfejs
     */
    public void setImgCaptureCreatedListener(GetCamInterface interf) {
        doItLaterIntf = interf;
    }

    Timer timer = new Timer();

    SRTWriter srtWriter;

    /**
     * Jest to metoda odpowiadająca za funkcję obsługi nagrywania video.
     * @param option Opcja
     */
    @SuppressLint({"RestrictedApi", "MissingPermission"})
    public void takeVideo(boolean option) {
        if(!option) {
         //   timer.cancel();
            srtWriter.stopWriting();
            videoCapture.stopRecording();
        }
        else if(option)
        {
            TimerTask task = new TimerTask() {
                int count = 0;
                /**
                 * Jest to metoda odpowiadająca za uruchamianie procesu nagrywania.
                 */
                public void run() {
                    if(count == 0)
                    {
                        File file = new FileHandler(main).createVideo("mp4");
                        VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(file).build();
                        if(videoCapture == null)
                            Log.e("CamAccess", ">>> videoCapture jest null!");
                        videoCapture.startRecording(outputFileOptions, ContextCompat.getMainExecutor(main), new VideoCapture.OnVideoSavedCallback() {

                            /**
                             * Jest to metoda służąca do informacji o zapisie video.
                             * @param outputFileResults Wyniki pliku wyjściowego
                             */
                            @Override
                            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                                System.out.println("----------ZapisywanieVID----------");
                                Log.i("CamAccess", ">>> Zapisano nagranie");
                            }

                            /**
                             * Jest to metoda służąca do informacji o wystąpionym błędzie przy zapisie video.
                             * @param videoCaptureError Kod błędu
                             * @param message Treść błędu
                             * @param cause Przyczyna błędu
                             */
                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                System.out.println("----------ErrorVID----------");
                                Log.e("CamAccess", ">>> Nie udało się zapisać nagrania");
                            }
                        });
                        createSRT();
                    }
                    DataHolder dataHolder = DataHolder.getInstance();
                    dataHolder.setCounter(String.valueOf(count));
                    count++;
                    /* TODO settings - dlugosc nagrania */
                    //System.out.println("Czas: " + count + " sekund");
                    //10+2 -> 2 to opoznienie aby nagrac film 10 sekundowy
                    long limit = getLimitLength();
                    if (count >= limit) {
                        videoCapture.stopRecording();
                        srtWriter.stopWriting();
                        count = 0;
                    }
                }
            };
           try {
               timer.schedule(task, 0, 1000);
           } catch(Exception e)
           {
               Log.v("CANCEL", ">>> Timer anulowany");
           }
           }
        }
     // end of takeVideo()

    private void createSRT () {
        File srtFile = new FileHandler(main).createDataFile("srt");
        srtWriter = new SRTWriter(srtFile);
        srtWriter.start();
    }

    /**
     * Przestań pisać do pliku srt, wywoływane w MainService.onDestroy()
     */
    public void stopSRT() {
        srtWriter.stopWriting();
    }

}
