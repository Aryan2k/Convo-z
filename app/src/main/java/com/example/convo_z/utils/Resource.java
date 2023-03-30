package com.example.convo_z.utils;

public class Resource<Data> {

    RequestStatus status;
    Data data;
    String message;

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Resource(RequestStatus status, Data data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public Resource<Data> success(Data data) {
        return new Resource<>(RequestStatus.SUCCESS, data, null);
    }

    public Resource<Data> loading(Data data) {
        return new Resource<>(RequestStatus.LOADING, data, null);
    }

    public Resource<Data> exception(Data data, String msg) {
        return new Resource<>(RequestStatus.EXCEPTION, data, msg);
    }

}