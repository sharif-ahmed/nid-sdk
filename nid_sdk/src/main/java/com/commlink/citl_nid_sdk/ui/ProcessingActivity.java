package com.commlink.citl_nid_sdk.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.commlink.citl_nid_sdk.R;
import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.db.NidDatabase;
import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.model.NIDInfo;
import com.commlink.citl_nid_sdk.model.NidInfoEntity;
import com.commlink.citl_nid_sdk.model.NidVerifyRequest;
import com.commlink.citl_nid_sdk.model.NidVerifyResponse;
import com.commlink.citl_nid_sdk.network.ApiClient;
import com.commlink.citl_nid_sdk.network.NidApiService;
import com.commlink.citl_nid_sdk.utils.BitmapHolder;
import com.commlink.citl_nid_sdk.utils.BitmapUtils;
import com.commlink.citl_nid_sdk.utils.CallbackHolder;
import com.commlink.citl_nid_sdk.utils.EncryptionUtil;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView statusText;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        //new OcrAndMatchTask().execute(nid, selfie);
        runOcrAndMatch(nid, selfie);
    }
/*

    private class OcrAndMatchTask extends AsyncTask<Bitmap, String, ResultHolder> {
        private final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        @Override
        protected ResultHolder doInBackground(Bitmap... bitmaps) {
            Bitmap nid = bitmaps[0];
            Bitmap selfie = bitmaps[1];
            final ResultHolder resultHolder = new ResultHolder();

            publishProgress(getString(R.string.nid_processing_ocr));
            
            NIDOCRProcessor ocr = new NIDOCRProcessor(ProcessingActivity.this);
            ocr.process(nid, new NIDOCRProcessor.Callback() {
                @Override
                public void onSuccess(NIDInfo info) {
                    resultHolder.nidInfo = info;
                    
                    // Proceed to local face matching
                    publishProgress(getString(R.string.nid_processing_face_match));
                    
                    try {
                        FaceNet faceNet = new FaceNet(ProcessingActivity.this);
                        float[] nidEmb = faceNet.getEmbedding(nid);
                        float[] selfieEmb = faceNet.getEmbedding(selfie);
                        faceNet.close();

                        float score = Similarity.cosineSimilarity(nidEmb, selfieEmb);
                        resultHolder.matchScore = score;
                        resultHolder.match = score > 0.35f; // Threshold logic

                        resultHolder.success = true;
                    } catch (Exception e) {
                        resultHolder.error = new NIDError(
                                NIDError.Code.FACE_MATCH_FAILED,
                                "Face match failed", e);
                        resultHolder.success = false;
                    }
                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    resultHolder.error = new NIDError(
                            NIDError.Code.OCR_ERROR, "OCR failed", e);
                    resultHolder.success = false;
                    latch.countDown();
                }
            });

            try {
                // Wait for extraction and matching to complete (max 30s)
                latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                resultHolder.error = new NIDError(NIDError.Code.UNKNOWN, "Processing interrupted");
            }

            return resultHolder;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            statusText.setText(values[0]);
        }

        @Override
        protected void onPostExecute(ResultHolder result) {
            if (!result.success || result.error != null) {
                NIDCallback cb = CallbackHolder.getInstance().getCallback();
                if (cb != null) {
                    cb.onError(result.error != null ? result.error : 
                            new NIDError(NIDError.Code.UNKNOWN, "Processing failed"));
                }
                finish();
                return;
            }

            // Successfully processed, move to Result screen
            ResultActivity.start(ProcessingActivity.this, result.match, 
                    result.matchScore, result.nidInfo);
            finish();
        }
    }

    private void runOcrAndMatch(Bitmap nid, Bitmap selfie) {

        executorService.execute(() -> {

            ResultHolder resultHolder = new ResultHolder();
            CountDownLatch latch = new CountDownLatch(1);

            // Update UI
            mainHandler.post(() ->
                    statusText.setText(getString(R.string.nid_processing_ocr)));

            NIDOCRProcessor ocr = new NIDOCRProcessor(ProcessingActivity.this);

            ocr.process(nid, new NIDOCRProcessor.Callback() {

                @Override
                public void onSuccess(NIDInfo info) {

                    resultHolder.nidInfo = info;

                    mainHandler.post(() ->
                            statusText.setText(getString(R.string.nid_processing_face_match)));

                */
/*
                try {

                    FaceNet faceNet = new FaceNet(ProcessingActivity.this);

                    float[] nidEmb = faceNet.getEmbedding(nid);
                    float[] selfieEmb = faceNet.getEmbedding(selfie);

                    faceNet.close();

                    float score = Similarity.cosineSimilarity(nidEmb, selfieEmb);

                    resultHolder.matchScore = score;
                    resultHolder.match = score > 0.35f;

                    resultHolder.success = true;

                } catch (Exception e) {

                    resultHolder.error = new NIDError(
                            NIDError.Code.FACE_MATCH_FAILED,
                            "Face match failed", e);

                    resultHolder.success = false;
                }
                *//*


                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {

                    resultHolder.error = new NIDError(
                            NIDError.Code.OCR_ERROR,
                            "OCR failed", e);

                    resultHolder.success = false;

                    latch.countDown();
                }
            });


            try {

                latch.await(30, TimeUnit.SECONDS);

            } catch (InterruptedException e) {

                resultHolder.error = new NIDError(
                        NIDError.Code.UNKNOWN,
                        "Processing interrupted");
            }


            // Return result to UI Thread
            mainHandler.post(() -> {

                if (!resultHolder.success || resultHolder.error != null) {

                    NIDCallback cb = CallbackHolder.getInstance().getCallback();

                    if (cb != null) {

                        cb.onError(resultHolder.error != null
                                ? resultHolder.error
                                : new NIDError(NIDError.Code.UNKNOWN, "Processing failed"));
                    }

                    finish();

                    return;
                }

                ResultActivity.start(
                        ProcessingActivity.this,
                        resultHolder.match,
                        resultHolder.matchScore,
                        resultHolder.nidInfo);

                finish();

            });

        });

    }
*/

    private void runOcrAndMatch(Bitmap nid, Bitmap selfie) {

        executorService.execute(() -> {

            ResultHolder resultHolder = new ResultHolder();

            final Object lock = new Object();

            // publishProgress replacement
            /*mainHandler.post(() -> statusText.setText(getString(R.string.nid_processing_ocr)));
            NIDOCRProcessor ocr = new NIDOCRProcessor(ProcessingActivity.this);
            ocr.process(nid, new NIDOCRProcessor.Callback() {
                @Override
                public void onSuccess(NIDInfo info) {
                    resultHolder.nidInfo = info;
                    try {
                        mainHandler.post(() -> statusText.setText(getString(R.string.nid_processing_face_match)));
                    *//*
                    FaceNet faceNet =
                            new FaceNet(ProcessingActivity.this);

                    float[] nidEmb =
                            faceNet.getEmbedding(nid);

                    float[] selfieEmb =
                            faceNet.getEmbedding(selfie);

                    faceNet.close();

                    float score =
                            Similarity.cosineSimilarity(nidEmb, selfieEmb);

                    resultHolder.matchScore = score;

                    resultHolder.match = score > 0.8f;
                    *//*

                        resultHolder.success = true;

                    } catch (Exception e) {
                        resultHolder.error = new NIDError(NIDError.Code.FACE_MATCH_FAILED, "Face match failed", e);
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }
                @Override
                public void onError(Exception e) {
                    resultHolder.error = new NIDError(NIDError.Code.OCR_ERROR, "OCR failed", e);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });*/
            synchronized (lock) {
                try {
                    lock.wait(30000);
                } catch (InterruptedException e) {
                    resultHolder.error = new NIDError(NIDError.Code.OCR_ERROR, "OCR timeout", e);
                }
            }
            // onPostExecute replacement
            mainHandler.post(() -> {
                if (!resultHolder.success) {
                    if (resultHolder.error != null) {
                        NIDCallback cb = CallbackHolder.getInstance().getCallback();
                        if (cb != null)
                            cb.onError(resultHolder.error);
                    }
                    finish();
                    return;
                }

                // Successfully processed, move to Result screen
                NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
                executor.execute(() -> db.nidInfoDao().deleteAll());
                ResultActivity.start(
                        ProcessingActivity.this,
                        resultHolder.nidInfo
                );

                // call your server
                sendToServer(resultHolder);

            });


        });

    }

    private void sendToServer(ResultHolder result) {
        statusText.setText(R.string.nid_processing_network);

        NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
        final String deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        executor.execute(() -> {
            List<NidInfoEntity> entities = db.nidInfoDao().getAll();
            String frontBase64 = "";
            String backBase64 = "";

            if (!entities.isEmpty()) {
                NidInfoEntity entity = entities.get(entities.size() - 1);
                if (entity.getFrontImagePath() != null) {
                    Bitmap frontBitmap = BitmapFactory.decodeFile(entity.getFrontImagePath());
                    frontBase64 = BitmapUtils.toBase64(frontBitmap);
                }
                if (entity.getBackImagePath() != null) {
                    Bitmap backBitmap = BitmapFactory.decodeFile(entity.getBackImagePath());
                    backBase64 = BitmapUtils.toBase64(backBitmap);
                }
            }

            String selfieBase64 = BitmapUtils.toBase64(BitmapHolder.getSelfieBitmap());

            NidVerifyRequest request = new NidVerifyRequest(
                    result.nidInfo.getNidNumber(),
                    result.nidInfo.getDateOfBirth(),
                    selfieBase64,
                    frontBase64,
                    backBase64,
                    deviceId
            );

            processAndSend(request, result);
        });
    }

    private void processAndSend(NidVerifyRequest request, ResultHolder result) {

        try {
            String json = new Gson().toJson(request);
            String licenseKey = CallbackHolder.getInstance().getLicenseKey();
            String encrypted = EncryptionUtil.encrypt(json, licenseKey);
            request.encrypted = encrypted;
        } catch (Exception e) {
            Log.w("ProcessingActivity", "Encryption failed, sending plain request", e);
        }

        runOnUiThread(() -> {
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
                    ResultActivity.start(
                            ProcessingActivity.this,
                            result.nidInfo
                    );
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
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private static class ResultHolder {
        boolean success;
        NIDInfo nidInfo;
        float matchScore;
        boolean match;
        NIDError error;
    }
}

