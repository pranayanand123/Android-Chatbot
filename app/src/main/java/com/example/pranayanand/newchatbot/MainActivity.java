package com.example.pranayanand.newchatbot;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;
    ImageButton btnSpeak;
    TextView txtSpeechInput, outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (((checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) &&(checkSelfPermission(Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED))){
                return;
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                        {
                                INTERNET,
                                RECORD_AUDIO
                        }, PERMISSION_REQUEST_CODE);

            }


        }


        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        outputText = (TextView) findViewById(R.id.outputTex);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0) {

                    boolean InternetPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (InternetPermission && RecordAudioPermission ) {

                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say Something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "orry! Your device doesn\\'t support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userQuery=result.get(0);
                    txtSpeechInput.setText(userQuery);
                    RetrieveFeedTask task=new RetrieveFeedTask();
                    task.execute(userQuery);


                }
                break;
            }

        }
    }
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... voids) {
            String s = null;
            try {

                s = GetText(voids[0]);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d("karma", "Exception occurred " + e);
            }

            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            outputText.setText(s);

        }
    }
    // Create GetText Metod
    public String GetText(String query) throws UnsupportedEncodingException {

        String text = "";
        BufferedReader reader = null;

        // Send data
        try {

            // Defined URL  where to send data
            URL url = new URL("https://api.api.ai/v1/query?v=20150910");

            // Send POST data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestProperty("Authorization", "Bearer 9b88056b72544b769f9dac344da68cc5");
            conn.setRequestProperty("Content-Type", "application/json");

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("query", query);
//            jsonParam.put("name", "order a medium pizza");
            jsonParam.put("lang", "en");
            jsonParam.put("sessionId", "1234567890");


            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            Log.d("karma", "after conversion is " + jsonParam.toString());
            wr.write(jsonParam.toString());
            wr.flush();
            Log.d("karma", "json is " + jsonParam);

            // Get the server response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;


            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }


            text = sb.toString();



            JSONObject object1 = new JSONObject(text);
            JSONObject object = object1.getJSONObject("result");
            JSONObject fulfillment = null;
            JSONArray messages = null;
            JSONObject message = null;
            String speech = null;
//            if (object.has("fulfillment")) {
            fulfillment = object.getJSONObject("fulfillment");
            messages = fulfillment.getJSONArray("messages");
            message = messages.getJSONObject(0);
            speech = message.optString("speech");


//                if (fulfillment.has("speech")) {
            //speech = fulfillment.optString("speech");
//                }
//            }


            Log.d("karma ", "response is " + text);
            return speech;

        } catch (Exception ex) {
            Log.d("karma", "exception at last " + ex);
        } finally {
            try {

                reader.close();
            } catch (Exception ex) {
            }
        }

        return null;
    }
}
