package ognora.drishti;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import java.util.List;

public class Myservice extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Speech.init(getApplicationContext(), getPackageName());
    }

    @Override
    public void onStart(Intent intent, int startId) {

     /*   try {
            Speech.getInstance().startListening(mcontext.speechview , new SpeechDelegate() {

                @Override
                public void onStartOfSpeech() {
                    Log.i("speech", "speech recognition is now active");
                }

                @Override
                public void onSpeechRmsChanged(float value) {
                    Log.d("speech", "rms is now: " + value);
                }

                @Override
                public void onSpeechPartialResults(List<String> results) {
                    StringBuilder str = new StringBuilder();
                    for (String res : results) {
                        str.append(res).append(" ");
                    }

                    Log.i("speech", "partial result: " + str.toString().trim());
                }

                @Override
                public void onSpeechResult(String result) {
                    if(result==null)
                        ((CameraActivity)mcontext).tryagain();
                    Log.i("speech", "result: " + result);

                    if(result.contains("click"))
                        ((CameraActivity)mcontext).clickPicture();
                    else if(result.contains("flash"))
                        ((CameraActivity)mcontext).flashtoggle();
                    else
                        ((CameraActivity)mcontext).tryagain();


                }
            });


        } catch (SpeechRecognitionNotAvailable exc) {
            Log.e("speech", "Speech recognition is not available on this device!");
            // You can prompt the user if he wants to install Google App to have
            // speech recognition, and then you can simply call:
            //
            // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
            //
            // to redirect the user to the Google App page on Play Store
        } catch (GoogleVoiceTypingDisabledException exc) {
            Log.e("speech", "Google voice typing must be enabled!");
        }*/

    }

    @Override
    public void onDestroy() {
        Speech.getInstance().shutdown();

    }
}
