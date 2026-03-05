package citl_nid_sdk;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TokenApiService {
    @POST("auth/token")
    Call<TokenResponse> getApiToken(@Body TokenRequest request);
}
