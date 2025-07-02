package dev.vtvinh24.ezquiz.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface PasteService {
    @GET
    Call<ResponseBody> getRawPaste(@Url String url);
}

