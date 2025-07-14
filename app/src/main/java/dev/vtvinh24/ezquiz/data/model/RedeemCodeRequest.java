package dev.vtvinh24.ezquiz.data.model;

public class RedeemCodeRequest {
    private String code;

    public RedeemCodeRequest(String code) {
        this.code = code;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
