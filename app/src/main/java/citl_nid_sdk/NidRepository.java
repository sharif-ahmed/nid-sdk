package citl_nid_sdk;

import android.content.Context;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for handling NID related API calls.
 */
public class NidRepository {

    private final NidApiService apiService;

    public NidRepository(Context context) {
        this.apiService = ApiClient.getService(context);
    }

    public interface VerificationCallback {
        void onResult(Result<NidFaceVerificationResponse> result);
    }

    /**
     * Perform NID Face Verification using the REST API.
     */
    public void verifyFace(
            String nidNumber,
            String nameEnglish,
            String dob,
            boolean isFaceMatchRequired,
            String photoBase64,
            final VerificationCallback callback
    ) {
        
        if (callback != null) callback.onResult(Result.loading());

        String transactionId = java.util.UUID.randomUUID().toString();
        
        NidFaceVerificationRequest request = new NidFaceVerificationRequest(
                nidNumber,
                nameEnglish,
                dob,
                isFaceMatchRequired,
                transactionId,
                photoBase64
        );

        apiService.verifyFace(BuildConfig.NID_API_KEY, request).enqueue(new retrofit2.Callback<NidFaceVerificationResponse>() {
            @Override
            public void onResponse(retrofit2.Call<NidFaceVerificationResponse> call, retrofit2.Response<NidFaceVerificationResponse> response) {
                if (callback == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Result.success(response.body()));
                } else {
                    callback.onResult(Result.error("API Error: " + response.code(), String.valueOf(response.code())));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<NidFaceVerificationResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onResult(Result.error("Network Failure: " + t.getMessage(), "NETWORK_ERROR"));
                }
            }
        });
    }
}
