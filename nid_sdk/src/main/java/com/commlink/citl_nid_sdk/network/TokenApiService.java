package com.commlink.citl_nid_sdk.network;

import com.commlink.citl_nid_sdk.model.TokenRequest;
import com.commlink.citl_nid_sdk.model.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TokenApiService {
    @POST("auth/token")
    Call<TokenResponse> getApiToken(@Body TokenRequest request);
}
