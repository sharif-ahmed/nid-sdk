package com.commlink.citl_nid_sdk.network;
import com.commlink.citl_nid_sdk.BuildConfig;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://your.api.server/";
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().create();

            // 1. Logging Interceptor (Debug only)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            // 2. Certificate Pinning (Replace with actual SHA-256 hashes)
            /*CertificatePinner certPinner = new CertificatePinner.Builder()
                    .add("your.api.server", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                    .build();*/

            // 3. Retry Interceptor
            Interceptor retryInterceptor = chain -> {
                Request request = chain.request();
                okhttp3.Response response = null;
                int retryCount = 0;
                while (retryCount < 3) {
                    try {
                        response = chain.proceed(request);
                        if (response.isSuccessful()) break;
                    } catch (Exception e) {
                        if (retryCount >= 2) throw e;
                    }
                    retryCount++;
                }
                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    //.addInterceptor(retryInterceptor)
                    //.certificatePinner(certPinner)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header("Accept", "application/json")
                                .method(original.method(), original.body());
                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static NidApiService getService(Context context) {
        return getClient(context).create(NidApiService.class);
    }
}

