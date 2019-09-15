package ognora.drishti;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Api {

    @Multipart
    @POST("/predict")
    Call<JsonObject> predict(
            @Part MultipartBody.Part file
            //, @Part("image") RequestBody requestBody
    );



}
