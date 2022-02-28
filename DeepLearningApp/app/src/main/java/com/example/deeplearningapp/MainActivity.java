package com.example.deeplearningapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.deeplearningapp.ml.ClassifierPlants;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button select, predict;
    private TextView tv;
    private Bitmap img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        select = (Button) findViewById(R.id.button);
        predict = (Button) findViewById(R.id.button2);
        tv = (TextView) findViewById(R.id.textView);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });
        predict.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                img = Bitmap.createScaledBitmap(img,224,224,true);
                try {
                    ClassifierPlants model = ClassifierPlants.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorImage image = TensorImage.fromBitmap(img);

                    // Runs model inference and gets result.
                    ClassifierPlants.Outputs outputs = model.process(image);
                    List<Category> probability = outputs.getProbabilityAsCategoryList();
                    probability.sort(Comparator.comparing(Category::getScore, Comparator.reverseOrder()));
                    String labels = String.valueOf(probability.get(0));
                    String name = labels.substring(labels.indexOf('"'), labels.indexOf("("));
                    String score = labels.substring(labels.lastIndexOf("="), labels.lastIndexOf(")")).substring(1);
                    System.out.println(probability.get(0));
                    tv.setText(String.valueOf("Plant "+name+"\n"+"Confidence "+score));
                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            imageView.setImageURI(data.getData());
            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}