package com.codephillip.app.ocrgooglevision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

/**
 * ABOUT:
 * (OCR) Extract text from image using Google Vision api
 *
 * NOTE:
 * Make sure the device has atleast 400mb of RAM
 *
 * CREDIT:
 * https://www.youtube.com/watch?v=f4HUUPs91kw
 * https://stackoverflow.com/questions/32099530/google-vision-barcode-library-not-found/32123937#32123937
 * */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OCR";
    private Button takePicture;
    private ImageView picture;
    private TextView textView;

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
                processImage();
            }
        });


    }

    // may need to do this in a different thread(Async Task)
    private void processImage() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            Log.d(TAG, "processImage: started");

            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.credit_card);
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
