package org.veloscope.json;

public class Result<T> {
    private T data;
    private String status;
    private String error;
    private int errorCode;

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> dto = new Result<T>();
        dto.setStatus("ok");
        dto.setData(data);
        return dto;
    }

    public static <T> Result<T> error(int code, String error) {
        Result<T> dto = new Result<T>();
        dto.setStatus("error");
        dto.setErrorCode(code);
        dto.setError(error);
        return dto;
    }


    public static <T> Result<T> badRequest() {
        return error(400, "Bad Request");
    }

    public static <T> Result<T> unauthorized() {
        return error(401, "Unauthorized");
    }

    public static <T> Result<T> forbidden() {
        return error(403, "Forbidden");
    }

    public static <T> Result<T> notFound() {
        return error(404, "Not Found");
    }
}
