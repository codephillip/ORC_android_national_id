package com.codephillip.app.ocrgooglevision;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ABOUT:
 * (OCR) Extract text from image using Google Vision api (works with Internet)
 * And play-services-vision api (works locally)
 * <p>
 * NOTE:
 * Make sure the device has atleast 400mb of RAM
 * Atleast 3GB of storage
 * Device must have Google Play Services installed
 * <p>
 * CREDIT:
 * https://www.youtube.com/watch?v=f4HUUPs91kw
 * https://stackoverflow.com/questions/32099530/google-vision-barcode-library-not-found/32123937#32123937
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OCR";
    private Button takePicture;
    private ImageView picture;
    private TextView textView;
    private Bitmap bmp;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    private ProgressDialog pDialog;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePicture = (Button) findViewById(R.id.take_picture);
        picture = (ImageView) findViewById(R.id.picture);
        textView = (TextView) findViewById(R.id.text);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
//                processImageOnline();
//                processImage();
            }
        });
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    //called after captureImage()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bmp = (Bitmap) extras.get("data");
            bitmap = (Bitmap) extras.get("data");
            picture.setImageBitmap(bmp);
            processImage();

            Log.d(TAG, "onActivityResult: starting async task");
//            AsyncTaskRunner runner = new AsyncTaskRunner();
//            runner.execute();
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String response;

        @Override
        protected String doInBackground(String... params) {
            try {
                return processImageOnline();
            } catch (Exception e) {
                e.printStackTrace();
                response = e.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: " + result);
            extractJsonData(result);
            dismissProgressDialog();
        }

        @Override
        protected void onPreExecute() {
            displayProgressDialog();
        }
    }

    private void extractJsonData(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String description = jsonObject
                    .getJSONArray("responses").getJSONObject(0)
                    .getJSONArray("textAnnotations").getJSONObject(0)
                    .getString("description");

            String[] parts = description.split("\n");
            String lastName = parts[2];
            String firstName = parts[3];
            String nin = parts[5].substring(0, 15);

            Log.d(TAG, "extractJsonData: " + lastName + firstName + nin);
            Log.d(TAG, "extractJsonData: " + description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String processImageOnline() {
        String server_data = "{\n" +
                "  \"requests\": [\n" +
                "    {\n" +
                "      \"image\": {\n" +
                "        \"source\": {\n" +
                "          \"imageUri\": \"http://entebbenews.com/wp-content/uploads/2015/11/The-late-Mark-Kasozi-while-still-alive-331x219.png\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"features\": [\n" +
                "        {\n" +
                "          \"type\": \"TEXT_DETECTION\",\n" +
                "          \"maxResults\": 1\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        String jsonString = null;
        try {
            jsonString = post("https://vision.googleapis.com/v1/images:annotate?key=AIzaSyDbCP4W2Ru2qxZsGgrR5inBbOOf0X0S54s", server_data);
            Log.d("JSON#", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public String post(String url, String json) throws IOException {
        Log.d("JSON#", "okhttpCall");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void displayProgressDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    // may need to do this in a different thread(Async Task)
    private void processImage() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            Log.d(TAG, "processImage: started");

            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.national);

            picture.setImageBitmap(bitmap);

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                TextBlock textBlock = items.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            textView.setText(stringBuilder.toString());
        } else {
            Log.d(TAG, "processImage: not operational");
        }
    }
}
