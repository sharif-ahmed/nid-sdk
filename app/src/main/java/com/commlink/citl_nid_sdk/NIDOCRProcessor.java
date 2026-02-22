package com.commlink.citl_nid_sdk;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NIDOCRProcessor {

    public interface Callback {
        void onSuccess(NIDInfo info);
        void onError(Exception e);
    }

    private final Context context;

    public NIDOCRProcessor(Context context) {
        this.context = context.getApplicationContext();
    }

    public void process(Bitmap nidBitmap, Callback callback) {
        try {
            InputImage image = InputImage.fromBitmap(nidBitmap, 0);
            com.google.mlkit.vision.text.TextRecognizer recognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            Task<Text> task = recognizer.process(image);
            task.addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    String fullText = text.getText();
                    String nidNumber = extractNid(fullText);
                    String dob = extractDob(fullText);
                    String name = extractName(fullText);

                    NIDInfo info = new NIDInfo(nidNumber, name, dob);
                    info.setNidFrontImage(nidBitmap);
                    callback.onSuccess(info);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e);
                }
            });

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private String extractNid(String text) {
        Pattern p = Pattern.compile("\\b(\\d{10}|\\d{13}|\\d{17})\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private String extractDob(String text) {
        Pattern p = Pattern.compile("\\b(\\d{2}[-/]\\d{2}[-/]\\d{4})\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private String extractName(String text) {
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].toLowerCase();
            if (l.contains("name") || l.contains("নাম")) {
                if (i + 1 < lines.length) {
                    return lines[i + 1].trim();
                }
            }
        }
        return "";
    }
}

