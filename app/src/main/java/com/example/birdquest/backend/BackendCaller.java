package com.example.birdquest.backend;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BackendCaller {

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public void callBackend(String imageUrl, File outputFile, CallbackResult callbackResult) {
        String backendUrl = "http://192.168.0.102:8000/process/";
        RequestBody formBody = new FormBody.Builder()
                .add("url", imageUrl)
                .build();

        Request request = new Request.Builder()
                .url(backendUrl) // e.g. "http://192.168.0.102:8000/process/"
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("BACKEND_ERROR", "Failed to connect: " + e.getMessage());
                callbackResult.onComplete(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BACKEND_ERROR", "Server error: " + response.code());
                    callbackResult.onComplete(false);
                    return;
                }

                byte[] bytes = response.body().bytes();
                if (outputFile.exists()) {
                    boolean deleted = outputFile.delete();
                    if (!deleted) {
                        Log.e("FILE_E", "Failed to delete existing file");
                        callbackResult.onComplete(false);
                        return;
                    }
                }
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("FILE_E", "Failed to write: " + response.code());
                    callbackResult.onComplete(false);
                    return;
                }

                callbackResult.onComplete(true);
            }
        });
    }

    public interface CallbackResult {
        void onComplete(boolean success);
    }
}
