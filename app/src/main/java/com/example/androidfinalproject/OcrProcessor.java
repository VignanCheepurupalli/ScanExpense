package com.example.androidfinalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import androidx.annotation.NonNull;

import java.io.IOException;

public class OcrProcessor {

    public interface OcrCallback {
        void onTextExtracted(String result);
        void onError(Exception e);
    }

    public static void extractTextFromImage(Context context, Uri imageUri, OcrCallback callback) {
        Bitmap bitmap;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text visionText) {
                            callback.onTextExtracted(visionText.getText());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError(e);
                        }
                    });

        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
