package ognora.drishti;

import com.google.gson.JsonObject;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {

    @Multipart
    @POST("/predict")
    Call<JsonObject> predict(
            @Part MultipartBody.Part file

    );



}
