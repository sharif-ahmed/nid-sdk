package citl_nid_sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A generic wrapper class that represents the state of an operation (Success, Error, or Loading).
 */
public class Result<T> {
    public enum Status {
        SUCCESS, ERROR, LOADING
    }


    @SerializedName("isError")
    @Expose
    private Boolean isError;
    @SerializedName("errorMsg")
    @Expose
    private String errorMsg;
    @SerializedName("statusCode")
    @Expose
    private Integer statusCode;

    public final Status status;
    public T data;
    public final String message;
    public final String errorCode;

    private Result(Status status, T data, String message, String errorCode) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null, null);
    }

    public static <T> Result<T> error(String message, String errorCode) {
        return new Result<>(Status.ERROR, null, message, errorCode);
    }

    public static <T> Result<T> error(T data,String message, String errorCode) {
        return new Result<>(Status.ERROR, data, message, errorCode);
    }

    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null, null);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public Boolean getIsError() {
        return isError;
    }

    public void setIsError(Boolean isError) {
        this.isError = isError;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
