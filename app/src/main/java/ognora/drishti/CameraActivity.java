package ognora.drishti;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import net.gotev.speech.Logger;
import net.gotev.speech.Speech;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CameraActivity extends AppCompatActivity {

    Camera camera;
    FrameLayout cameraPreview;
    ImageView flash, click, flip;
    int PERMISSION = 101;

    ShowCamera showCamera ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.camera);
        flash = findViewById(R.id.flash);
        click = findViewById(R.id.click);
        flip = findViewById(R.id.flip);

        Speech.init(this, getPackageName());
        Logger.setLogLevel(Logger.LogLevel.DEBUG);

        checkPermission();

    }


    @Override
    protected void onResume() {
        super.onResume();
        openCamera();




    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
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
                    //send to ml using retrofit
               /* HashMap<String ,Object> map = new HashMap<>();
                map.put("image",pictureFile );*/

                    Api api = ApiClient.getClient().create(Api.class);
                    Call<JsonObject> call = api.predict(pictureFile);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            if (response.isSuccessful()) {
                                String prediction ="";
                                if(response.body().get("success").getAsBoolean()) {
                                   JsonObject obj = response.body().get("predictions").getAsJsonObject();

                                   prediction = obj.get("label").getAsString();
                                    Toast.makeText(CameraActivity.this, prediction + "note", Toast.LENGTH_LONG).show();

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

    // when capture button is clicked
    public void captureImage(View V)
    {
        if(camera!=null)
        {
            camera.takePicture(null, null, pictureCallback);
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
    // flash
    //camera switch



}
