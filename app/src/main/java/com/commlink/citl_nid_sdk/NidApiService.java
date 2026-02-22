package com.commlink.citl_nid_sdk;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NidApiService {

    @POST("nid/verify")
    Call<NidVerifyResponse> verify(@Body NidVerifyRequest request);
}

