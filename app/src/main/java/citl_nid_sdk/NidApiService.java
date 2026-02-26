package citl_nid_sdk;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NidApiService {

    @POST("nid/verify")
    Call<NidVerifyResponse> verify(@Body NidVerifyRequest request);

    @POST("nid/ec-validate")
    Call<SdkResponse> validateEC(@Body EcRequest request);

    @POST("nid/face-match")
    Call<SdkResponse> matchFace(@Body FaceMatchRequest request);
}

