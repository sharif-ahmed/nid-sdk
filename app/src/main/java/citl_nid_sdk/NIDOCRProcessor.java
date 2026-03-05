package citl_nid_sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import citl_nid_sdk.parser.NidParserEngine;
import citl_nid_sdk.parser.models.NidData;

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

            /*com.google.mlkit.vision.text.TextRecognizer recognizer =
                    TextRecognition.getClient(DevanagariTextRecognizerOptions.DEFAULT_OPTIONS);*/

            com.google.mlkit.vision.text.TextRecognizer recognizer = TextRecognition.getClient(
                    new DevanagariTextRecognizerOptions.Builder().build());

            Task<Text> task = recognizer.process(image);
            task.addOnSuccessListener(text -> {
                String fullText = text.getText();
                NidData nidData = NidParserEngine.parse(fullText);
                Log.d("NIDOCRProcessor", nidData.toString());
                /*String nidNumber = extractNid(fullText);
                String dob = extractDob(fullText);
                String name = extractName(fullText);*/
                String textNorm = BangladeshNidParser.normalize(fullText);
                //String nidNumber = BangladeshNidParser.extractNid(textNorm);
                //String nidNumber = NidDocumentParser.parse(fullText).nid_number;
                String nidNumber = nidData.getNidNumber();
                //String dob = BangladeshNidParser.extractDOB(textNorm);
                //String dob = NidDocumentParser.parse(fullText).date_of_birth;
                String dob = nidData.getDateOfBirth();
                //String name = BangladeshNidParser.extractName(textNorm);
                //String name = NidDocumentParser.parse(fullText).name_en;
                String name = nidData.getNameEn();
                //String nameBangla = BangladeshNidParser.extractBanglaName(textNorm);
                //String nameBangla = NidDocumentParser.parse(fullText).name_bn;
                String nameBangla = nidData.getNameBn();
                //String fatherName = BangladeshNidParser.extractFatherName(textNorm);
                //String fatherNameBangla = BangladeshNidParser.extractBanglaFatherName(textNorm);
                //String fatherNameBangla = NidDocumentParser.parse(fullText).father_name;
                String fatherNameBangla = nidData.getFatherName();
                //String motherName = BangladeshNidParser.extractMotherName(textNorm);
                //String motherNameBangla = BangladeshNidParser.extractBanglaMotherName(textNorm);
                //String motherNameBangla = BangladeshNIDParse.extractMotherName(fullText);
                //String motherNameBangla = NidDocumentParser.parse(fullText).mother_name;
                String motherNameBangla = nidData.getMotherName();
                String addressBangla = BangladeshNidParser.extractBanglaAddress(textNorm);

                NIDInfo info = new NIDInfo(nidNumber, name, dob);
                info.setNameBangla(nameBangla);
                //info.setFatherName(fatherName);
                info.setFatherNameBangla(fatherNameBangla);
                //info.setMotherName(motherName);
                info.setMotherNameBangla(motherNameBangla);
                info.setAddressBangla(addressBangla);
                info.setNidFrontImage(nidBitmap);
                info.setOcrRawData(fullText);
                callback.onSuccess(info);
            }).addOnFailureListener(e -> callback.onError(new Exception(NIDError.E100 + ": " + e.getMessage())));

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private String extractNid(String text) {
        /*Pattern p = Pattern.compile("\\b(\\d{10}|\\d{13}|\\d{17})\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "";*/
        Pattern pattern = Pattern.compile("\\d{3}\\s\\d{3}\\s\\d{4}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private String extractDob(String text) {
        /*Pattern p = Pattern.compile("\\b(\\d{2}[-/]\\d{2}[-/]\\d{4})\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "";*/
        Pattern pattern = Pattern.compile("Date of Birth\\s*([0-9]{1,2}\\s\\w{3}\\s[0-9]{4})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private String extractName(String text) {
        /*String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].toLowerCase();
            if (l.contains("name") || l.contains("নাম")) {
                if (i + 1 < lines.length) {
                    return lines[i + 1].trim();
                }
            }
        }
        return "";*/

        Pattern pattern = Pattern.compile("Name\\s*\\n?\\s*([A-Z ]+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }

    public boolean containsGovText(String text) {
        return text.contains("Government of the People's Republic of Bangladesh");
    }


    public boolean containsNationalId(String text) {
        return text.contains("National ID Card");
    }
}

