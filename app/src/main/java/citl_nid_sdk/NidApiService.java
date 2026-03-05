package citl_nid_sdk;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NidApiService {

    @POST("nid/verify")
    Call<NidVerifyResponse> verify(@Body NidVerifyRequest request);

    /*
    * SDK API
    * */
    @POST("api/nid-face-verification")
    Call<NidEcVerifyResponse> validateEC(
            @retrofit2.http.Header("x-api-key") String apiKey,
            @Body NidECVerifyRequest request
    );

    @POST("api/nid-face-verification")
    Call<NidFaceVerificationResponse> verifyFace(
            @retrofit2.http.Header("x-api-key") String apiKey,
            @Body NidFaceVerificationRequest request
    );
}

