package ognora.drishti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import java.util.List;

public class SpeechActivity extends AppCompatActivity {

    SpeechDelegate speechDelegate;
    net.gotev.speech.ui.SpeechProgressView speechview ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        speechview = findViewById(R.id.progress) ;

        speechDelegate = new SpeechDelegate() {

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
                Speech.getInstance().stopListening();
                if(result==null)
                   return;
                Log.i("speech", "result: " + result);

                /*Intent intent = new Intent();
                intent.putExtra("key", result);
*/
                SpeechDataHolder.setData(result);
                finish();
            }
        };

        Speech.init(this, getPackageName());

        voiceRecognition();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().stopListening();
        Speech.getInstance().shutdown();
    }


    public void voiceRecognition() {

        try {
            Speech.getInstance().startListening(speechview, speechDelegate);


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
        }
    }

}