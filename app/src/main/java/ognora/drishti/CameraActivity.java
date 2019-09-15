package ognora.drishti;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CameraActivity extends AppCompatActivity{

    Camera camera;
    FrameLayout cameraPreview;
    ImageView flash, click, flip;
    int PERMISSION = 101;
    net.gotev.speech.ui.SpeechProgressView speechview ;

    ShowCamera showCamera ;

    private boolean isFlashOn = false;
    boolean hasCameraFlash;
    Camera.Parameters params ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.camera);
        flash = findViewById(R.id.flash);
        click = findViewById(R.id.click);
        flip = findViewById(R.id.flip);
        speechview = findViewById(R.id.progress) ;

     //    final MediaPlayer mp = MediaPlayer.create(this, R.raw.soho);

         hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        Logger.setLogLevel(Logger.LogLevel.DEBUG);

        checkPermission();

    }


    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
        params = camera.getParameters();

        startService(new Intent(this, Myservice.class));

      //  voiceRecognition();

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA )
                != PackageManager.PERMISSION_GRANTED  && (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED )) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PERMISSION);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION && grantResults.length>0 ) {

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            && grantResults[2]==PackageManager.PERMISSION_GRANTED){
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
        flashtoggle();
    }



    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            //when picture is taken

           // File pictureFile = getOutputMediaFile();
            File pictureFile = getFileFromBytes(bytes);

            if (pictureFile==null)
                return;
            else
            {
             /*       //send to ml using retrofit

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
                                      Toast.makeText(CameraActivity.this, "Try again", Toast.LENGTH_LONG).show();
                                  else
                                    Toast.makeText(CameraActivity.this, prediction + " note", Toast.LENGTH_LONG).show();

                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });*/

                    camera.startPreview();

            }
        }
    };

    private File getOutputMediaFile() {

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

    public File getFileFromBytes(byte[] imageBytes){
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
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
        return imageFile;
    }


    // camera feature functions

    // click image
    public void clickPicture() {
        if(camera!=null)
        {
            camera.takePicture(null, null, pictureCallback);
        }
        startService(new Intent(this, Myservice.class));
    }

    // try again
    public void tryagain()
    {  Log.i("speech", "try again");
      //  voiceRecognition();
        startService(new Intent(this, Myservice.class));

    }

    // flash
    public void flashtoggle() {
        if (!hasCameraFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
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

    private void turnOffFlash() {

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


    }

    private  void turnOnFlash(){
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

    //Voice mute



}
