package com.commlink.citl_nid_sdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import citl_nid_sdk.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView statusText;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ProcessingActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        startProcessing();
    }

    private void startProcessing() {
        Bitmap nid = BitmapHolder.getNidBitmap();
        Bitmap selfie = BitmapHolder.getSelfieBitmap();
        if (nid == null || selfie == null) {
            NIDCallback cb = CallbackHolder.getInstance().getCallback();
            if (cb != null) {
                cb.onError(new NIDError(NIDError.Code.UNKNOWN,
                        "Missing images for processing"));
            }
            finish();
            return;
        }

        statusText.setText(R.string.nid_processing_ocr);
        new OcrAndMatchTask().execute(nid, selfie);
    }

    private class OcrAndMatchTask extends AsyncTask<Bitmap, String, ResultHolder> {

        @Override
        protected ResultHolder doInBackground(Bitmap... bitmaps) {
            Bitmap nid = bitmaps[0];
            Bitmap selfie = bitmaps[1];

            publishProgress(getString(R.string.nid_processing_ocr));
            NIDOCRProcessor ocr = new NIDOCRProcessor(ProcessingActivity.this);

            final ResultHolder resultHolder = new ResultHolder();

            final Object lock = new Object();

            ocr.process(nid, new NIDOCRProcessor.Callback() {
                @Override
                public void onSuccess(NIDInfo info) {
                    resultHolder.nidInfo = info;

                    try {
                        publishProgress(getString(R.string.nid_processing_face_match));

                        /*FaceNet faceNet = new FaceNet(ProcessingActivity.this);
                        float[] nidEmb = faceNet.getEmbedding(nid);
                        float[] selfieEmb = faceNet.getEmbedding(selfie);
                        faceNet.close();

                        float score = Similarity.cosineSimilarity(nidEmb, selfieEmb);
                        resultHolder.matchScore = score;
                        resultHolder.match = score > 0.8f;*/

                        resultHolder.success = true;
                    } catch (Exception e) {
                        resultHolder.error = new NIDError(
                                NIDError.Code.FACE_MATCH_FAILED,
                                "Face match failed", e);
                    }

                    synchronized (lock) {
                        lock.notify();
                    }
                }

                @Override
                public void onError(Exception e) {
                    resultHolder.error = new NIDError(
                            NIDError.Code.OCR_ERROR, "OCR failed", e);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            synchronized (lock) {
                try {
                    lock.wait(30000);
                } catch (InterruptedException e) {
                    resultHolder.error = new NIDError(
                            NIDError.Code.OCR_ERROR, "OCR timeout", e);
                }
            }
            return resultHolder;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            statusText.setText(values[0]);
        }

        @Override
        protected void onPostExecute(ResultHolder result) {
            if (!result.success) {
                if (result.error != null) {
                    NIDCallback cb = CallbackHolder.getInstance().getCallback();
                    if (cb != null) cb.onError(result.error);
                }
                finish();
                return;
            }

            sendToServer(result);
        }
    }

    private void sendToServer(ResultHolder result) {
        statusText.setText(R.string.nid_processing_network);

        String selfieBase64 = BitmapUtils.toBase64(BitmapHolder.getSelfieBitmap());
        String deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        NidVerifyRequest request = new NidVerifyRequest(
                result.nidInfo.getNidNumber(),
                result.nidInfo.getDateOfBirth(),
                selfieBase64,
                deviceId
        );

        try {
            String json = new Gson().toJson(request);
            String licenseKey = CallbackHolder.getInstance().getLicenseKey();
            String encrypted = EncryptionUtil.encrypt(json, licenseKey);
            request.encrypted = encrypted;
        } catch (Exception e) {
            Log.w("ProcessingActivity", "Encryption failed, sending plain request", e);
        }

        NidApiService service = ApiClient.getService(this);
        service.verify(request).enqueue(new Callback<NidVerifyResponse>() {
            @Override
            public void onResponse(Call<NidVerifyResponse> call,
                                   Response<NidVerifyResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    NIDCallback cb = CallbackHolder.getInstance().getCallback();
                    if (cb != null) {
                        cb.onError(new NIDError(NIDError.Code.NETWORK_ERROR,
                                "API error: " + response.code()));
                    }
                    finish();
                    return;
                }
                NidVerifyResponse resp = response.body();
                ResultActivity.start(ProcessingActivity.this, resp.match,
                        resp.score, result.nidInfo);
                finish();
            }

            @Override
            public void onFailure(Call<NidVerifyResponse> call, Throwable t) {
                NIDCallback cb = CallbackHolder.getInstance().getCallback();
                if (cb != null) {
                    cb.onError(new NIDError(NIDError.Code.NETWORK_ERROR,
                            "API failure", t));
                }
                finish();
            }
        });
    }

    private static class ResultHolder {
        boolean success;
        NIDInfo nidInfo;
        float matchScore;
        boolean match;
        NIDError error;
    }
}

