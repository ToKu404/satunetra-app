package com.example.satunetra.activities.unregistered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.satunetra.R;
import com.example.satunetra.helper.SpeechHelper;
import com.example.satunetra.helper.VoiceHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    //deklarasi
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private TextToSpeech tts;
    private TextView tvRegister;
    private ConstraintLayout clConfirmNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //mengatur layout
        clConfirmNext = findViewById(R.id.cl_confirm_next);
        tvRegister = findViewById(R.id.tv_register);

        //membaca data dari firebase
        clConfirmNext.setEnabled(false);
        readData();

        //mengecek izin record audio
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        //memanggil konfigurasi SR dan TTS
        configureSpeechRecognition();
        configureTextToSpeech();

        //mengatur on click listener
        clConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jika ditekan maka SR akan mulai mendengarkan
                speechRecognizer.startListening(speechIntent);
            }
        });
    }

    private void readData() {
        //membaca data dari firebase yang ada pada bot_message
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("bot_message");
        reference.child("welcome").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String bot_message = snapshot.child("1").getValue(String.class);
                tvRegister.setText(bot_message);
                //delay 1 detik kemudian mulai TTS untuk bot_message
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        startSpeak(bot_message);
                    }},1000);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void startSpeak(String string) {
        //mengubah teks jadi suara
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(string, TextToSpeech.QUEUE_ADD, null, "text");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        configureSpeechRecognition();
    }

    @Override
    public void onDestroy() {
        // Mematikan TTS dan SP
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null){
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    private void configureTextToSpeech() {
        //konfigurasi tts
        VoiceHelper helper = new VoiceHelper(this);
        tts = helper.getTts();
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                new Thread()
                {
                    public void run()
                    {
                        //ketika tts selesai maka SR akan otomtasi dimulai
                        RegisterActivity.this.runOnUiThread(new Runnable()
                        {

                            public void run()
                            {
                                clConfirmNext.setEnabled(true);
                                speechRecognizer.startListening(speechIntent);
                            }
                        });
                    }
                }.start();

            }

            @Override
            public void onError(String utteranceId) {

            }
        });
    }

    private void configureSpeechRecognition() {
        //instanca SR
        SpeechHelper helper = new SpeechHelper(this, 200);
        speechRecognizer = helper.getSpeechRecognizer();
        speechIntent = helper.getSpeechIntent();
        //mengatur Listener SR
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                speechRecognizer.stopListening();
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                //mengirim result
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                if(matches!=null){
                    string = matches.get(0);
                    endOfResult(string);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }

    private void endOfResult(String string) {
        String[] sub = string.toLowerCase().split(" ");
        for(String subString : sub){
            System.out.println(subString);
            //Jika string yang diucapkan adalah lanjutkan
            if(subString.equals("lanjutkan")){
                Intent openResult = new Intent(RegisterActivity.this, InputNameActivity.class);
                startActivity(openResult);
                finish();
                break;
            }
            //jika bukan otomatis mulai ulang SR
            else{
                speechRecognizer.startListening(speechIntent);
            }
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}