package com.commlink.citl_nid_sdk.network;

import android.content.Context;

import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.model.NidECVerifyRequest;
import com.commlink.citl_nid_sdk.model.NidEcVerifyResponse;
import com.commlink.citl_nid_sdk.model.NidFaceVerificationRequest;
import com.commlink.citl_nid_sdk.model.NidFaceVerificationResponse;
import com.commlink.citl_nid_sdk.model.NidVerifyRequest;
import com.commlink.citl_nid_sdk.model.NidVerifyResponse;
import com.commlink.citl_nid_sdk.model.Result;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NidNetworkRepository {

    private final NidApiService apiService;

    public NidNetworkRepository(Context context) {
        this.apiService = ApiClient.getService(context);
    }

    public void verify(NidVerifyRequest request, final RepositoryCallback<NidVerifyResponse> callback) {
        apiService.verify(request).enqueue(new Callback<NidVerifyResponse>() {
            @Override
            public void onResponse(Call<NidVerifyResponse> call, Response<NidVerifyResponse> response) {
                handleResponse(response, callback, NIDError.E102);
            }

            @Override
            public void onFailure(Call<NidVerifyResponse> call, Throwable t) {
                handleFailure(t, callback, NIDError.E102);
            }
        });
    }

    public void validateEC(String apiKey, NidECVerifyRequest request,
            final RepositoryCallback<NidEcVerifyResponse> callback) {
        apiService.validateEC(apiKey, request).enqueue(new Callback<NidEcVerifyResponse>() {
            @Override
            public void onResponse(Call<NidEcVerifyResponse> call, Response<NidEcVerifyResponse> response) {
                handleResponse(response, callback, NIDError.E102);
            }

            @Override
            public void onFailure(Call<NidEcVerifyResponse> call, Throwable t) {
                handleFailure(t, callback, NIDError.E102);
            }
        });
    }

    public void verifyFace(String apiKey, NidFaceVerificationRequest request,
            final RepositoryCallback<NidFaceVerificationResponse> callback) {
        apiService.verifyFace(apiKey, request).enqueue(new Callback<NidFaceVerificationResponse>() {
            @Override
            public void onResponse(Call<NidFaceVerificationResponse> call,
                    Response<NidFaceVerificationResponse> response) {
                handleResponse(response, callback, NIDError.E105);
            }

            @Override
            public void onFailure(Call<NidFaceVerificationResponse> call, Throwable t) {
                handleFailure(t, callback, NIDError.E105);
            }
        });
    }

    private <T> void handleResponse(Response<T> response, RepositoryCallback<T> callback, String timeoutCode) {
        int code = response.code();
        if (response.isSuccessful() && response.body() != null) {
            T body = response.body();

            // Handle internal error codes if the body is a known response type with a
            // Result object
            boolean isInternalError = false;
            String internalMsg = null;
            String internalCode = NIDError.E500;

            if (body instanceof NidEcVerifyResponse) {
                Result<?> res = ((NidEcVerifyResponse) body).getResult();
                if (res != null && (res.getIsError() != null && res.getIsError())) {
                    isInternalError = true;
                    internalMsg = res.getErrorMsg();
                    if (res.errorCode != null && !res.errorCode.isEmpty()) {
                        internalCode = res.errorCode;
                    }
                }
            } else if (body instanceof NidFaceVerificationResponse) {
                Result<?> res = ((NidFaceVerificationResponse) body).getResult();
                if (res != null && (res.getIsError() != null && res.getIsError())) {
                    isInternalError = true;
                    internalMsg = res.getErrorMsg();
                    if (res.errorCode != null && !res.errorCode.isEmpty()) {
                        internalCode = res.errorCode;
                    }
                }
            }

            if (isInternalError) {
                callback.onError(new NIDError(NIDError.Code.NETWORK_ERROR,
                        internalMsg != null ? internalMsg : "Internal Server Error",
                        internalCode));
            } else {
                callback.onSuccess(body);
            }
        } else {
            String message;
            String errorCode = NIDError.E500;

            switch (code) {
                case 401:
                    message = "Unauthorized: Invalid API Key or Session";
                    errorCode = NIDError.E101; // Mapping to NID/DOB Mismatch or similar but 401 is usually Auth
                    break;
                case 404:
                    message = "Service Not Found (404)";
                    break;
                case 500:
                    message = "Internal Server Error (500)";
                    break;
                default:
                    message = "API Error: " + code;
                    break;
            }
            callback.onError(new NIDError(NIDError.Code.NETWORK_ERROR, message, errorCode));
        }
    }

    private <T> void handleFailure(Throwable t, RepositoryCallback<T> callback, String timeoutCode) {
        if (t instanceof SocketTimeoutException) {
            callback.onError(new NIDError(NIDError.Code.NETWORK_ERROR, "Request Timeout", timeoutCode));
        } else {
            callback.onError(new NIDError(NIDError.Code.NETWORK_ERROR, t.getMessage(), NIDError.E500));
        }
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);

        void onError(NIDError error);
    }
}
