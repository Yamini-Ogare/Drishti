package ognora.drishti;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.TextToSpeechCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.os.HandlerCompat.postDelayed;

public class CameraActivity extends AppCompatActivity{

    private static final int START_SPEECH = 1000;
     Camera camera;
    FrameLayout cameraPreview;
    ImageView flash, click, speaker;
    int PERMISSION = 302;
     String TAG = "Error";
     TextView result;


    ShowCamera showCamera ;
    AudioManager audioManager;

     private boolean isFlashOn = false;
     boolean hasCameraFlash;
     Camera.Parameters params ;
     private boolean isSoundOn = true;
    MediaPlayer mp;
     Handler handler ;

     int MILISTART = 5000, MILIDOWN = 4500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.camera);
        flash = findViewById(R.id.flash);
        click = findViewById(R.id.click);
        speaker = findViewById(R.id.sound);
        result = findViewById(R.id.result);

        checkPermission();

     //    final MediaPlayer mp = MediaPlayer.create(this, R.raw.soho);

         hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        Logger.setLogLevel(Logger.LogLevel.DEBUG);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20 , 0);


        // Greetings when app is opened
        mp = MediaPlayer.create(this, R.raw.welcome);
        mp.start();
        result.setVisibility(View.VISIBLE);
        result.setText("  Welcome!! ");
        delay();


    }

    @Override
    protected void onStart() {
        super.onStart();
        openCamera();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(SpeechDataHolder.hasData())
        {
            compare(SpeechDataHolder.getData());        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==START_SPEECH && resultCode==RESULT_OK && data!= null){

        }
    }

    private void compare(String key ) {
        Toast.makeText(this,"You said: "+ key, Toast.LENGTH_SHORT).show();

        if(key.toLowerCase().contains("click"))
          clickPicture();
        else if(key.toLowerCase().contains("flash on"))
               turnOnFlash();
         else
             if(key.toLowerCase().contains("flash off"))
                 turnOffFlash();
         else if(key.toLowerCase().contains("flash"))
                  flashtoggle(this);
             else
                 if(key.toLowerCase().contains("sound"))
                         soundOn();
                 else
                     if(key.toLowerCase().contains("mute"))
                         soundOff();



    }


    private void openSpeech() {

        Intent intent  = new Intent(this,SpeechActivity.class);
        startActivityForResult(intent,START_SPEECH);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
        camera.release();


    }

    private void checkPermission() {

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA )
                != PackageManager.PERMISSION_GRANTED ) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED )) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PERMISSION);

        }
        else {
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION && grantResults.length>0 ) {

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            && grantResults[2]==PackageManager.PERMISSION_GRANTED){
               openCamera();
               return;
            }
            else
            {
                finish();
            }
        }
    }

    private void openCamera() {

        try {
            camera = Camera.open(); // attempt to get a Camera instance
            showCamera = new ShowCamera(this, camera);
            cameraPreview.addView(showCamera);
            params = camera.getParameters();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("Camera Error", e.getMessage());
        }


    }

    // when capture button is clicked click Listener
    public void captureImage(View V)
    {
        clickPicture();
    }

    // when flash icon is clicked
    public void flashButton(View view) {
        flashtoggle(this);
    }

    //when speaker is clicked
    public void sound(View view) {

        soundToggle();

    }

    //when mic button is clicked

    public void micButton(View view) {

        openSpeech();
    }


    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            //when picture is taken

            File pictureFile = getFileFromBytes(bytes);

            if (pictureFile==null)
                return;
            else
            {
                    //send to ml using retrofit

                    Api api = ApiClient.getClient().create(Api.class);


                // Create a request body with file and image media type
                RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), pictureFile);
                // Create MultipartBody.Part using file request-body,file name and part name
                MultipartBody.Part part = MultipartBody.Part.createFormData("image", pictureFile.getName(), fileReqBody);

                Call call = api.predict(part);


                call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            if (response.isSuccessful()) {
                                String prediction ="";
                                if(response.body().get("success").getAsBoolean()) {

                                      JsonArray jarray = response.body().get("predictions").getAsJsonArray();

                                        JsonObject obj = jarray.get(0).getAsJsonObject();
                                        prediction = obj.get("label").getAsString();

                                  if(prediction.equalsIgnoreCase("0"))
                                            tryagain();
                                  else {
                                    //  say(prediction + " Rupees");
                                      result.setVisibility(View.VISIBLE);
                                      result.setText("Rs. "+prediction);
                                       switchSound(Integer.parseInt(prediction));

                                    //  Toast.makeText(CameraActivity.this, "Rs. "+prediction, Toast.LENGTH_SHORT ).show();
                                  }

                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });

                    camera.startPreview();

            }
        }
    };



    private static File getOutputMediaFile() {

        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED))
            return null;
        else
        {
            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "Drishti");

            if(!folder_gui.exists())
                folder_gui.mkdirs();
           return new File(folder_gui, "temp.jpg");
        }

    }

    public  File getFileFromBytes(byte[] imageBytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        File imageFile = getOutputMediaFile();

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);

            //Quality reduced to 60% change as per required
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, os);

            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "Error writing bitmap", e);
        }
        return imageFile;
    }


    // try again
    public void tryagain()
    {  Log.i("speech", "try again");

        mp = MediaPlayer.create(this, R.raw.tryagain);
        mp.start();
        result.setVisibility(View.VISIBLE);
        result.setText("  Try Again :( ");
     delay();

    }

    //Text to speech

  /*  public void say (final String msg){

        Speech.init(this, getPackageName());
        if( msg!=null) {

            Speech.getInstance().say(msg, new TextToSpeechCallback() {
                @Override
                public void onStart() {
                    Log.i("speech", "speech started");
                    result.setText(msg);
                    result.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCompleted() {
                    Log.i("speech", "speech completed");
                    result.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    Log.i("speech", "speech error");
                }
            });
        }

        Speech.getInstance().stopTextToSpeech();
        Speech.getInstance().shutdown();

    }*/

    // camera feature functions
    // click image
    public  void clickPicture() {
        clickSound();
        if(camera!=null)
        {
            camera.takePicture(null, null, pictureCallback);
        }
    }

    // flash
    public  void flashtoggle(Context mcontext) {
        if (!hasCameraFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(mcontext)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    flash.setVisibility(View.GONE);
                }
            });
            alert.show();
        }
        else
        if (isFlashOn) {
            // turn off flash
            turnOffFlash();
        } else {
            // turn on flash
            turnOnFlash();
        }

    }

    private  void turnOffFlash() {
         buttonSound();
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;

            // changing button/switch image
            flash.setImageResource(R.drawable.flashoff);
        }

        openCamera();
    }

    private void turnOnFlash(){

        buttonSound();
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;

            // changing button/switch image
            flash.setImageResource(R.drawable.flashon);
        }


    }

    //Voice mute/un-mute

    private void soundToggle() {
        if(isSoundOn)
            soundOff();
        else
            soundOn();


    }

    private void soundOn() {
        buttonSound();
        isSoundOn = true;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20 , 0);
        speaker.setImageResource(R.drawable.sound);

    }

    private void soundOff(){
        buttonSound();
        isSoundOn = false;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0 , 0);
        speaker.setImageResource(R.drawable.mute);

    }




    // button SOund Effect
    public void buttonSound()
    {
        mp = MediaPlayer.create(this, R.raw.button);
        mp.start();
      delay();
    }

    public void clickSound()
    {
        mp = MediaPlayer.create(this, R.raw.click);
        mp.start();
        delay();

    }

    private void switchSound(int value) {

        switch (value){

            case 10 : mp = MediaPlayer.create(this, R.raw.ten); break;
            case 50 : mp = MediaPlayer.create(this, R.raw.fifty); break;
            case 100 : mp = MediaPlayer.create(this, R.raw.oneh); break;
            case 200 : mp = MediaPlayer.create(this, R.raw.twoh); break;
            case 500 : mp = MediaPlayer.create(this, R.raw.fiveh); break;
            case 2000 : mp = MediaPlayer.create(this, R.raw.twot); break;
            default: mp = MediaPlayer.create(this, R.raw.button); break;
        }
        mp.start();
       delay();
    }


   // Volume up button to trigger mic
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

         if (event.getAction() == KeyEvent.ACTION_DOWN){

             if(event.getKeyCode()==KeyEvent.KEYCODE_VOLUME_UP){
                 openSpeech();
                 return true;
             }
         }

        return super.dispatchKeyEvent(event);
    }

  void delay()
  {
      final MyCountDown timer = new MyCountDown(MILISTART, MILIDOWN, result, mp);
  }

}
