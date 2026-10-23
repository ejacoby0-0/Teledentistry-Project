
package com.example.teledentistry_project.data.repository;

import android.util.Log;

import com.example.teledentistry_project.data.model.receipt.Receipt;
import com.example.teledentistry_project.data.model.receipt.ReceiptCreateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptRepository {

    private static final String TAG = "ReceiptRepository";

    public void createReceipt(
            String authToken,
            ReceiptCreateRequest request,
            ReceiptCallback callback
    ) {
        PocketBaseClient.api.createReceipt(authToken, request)
                .enqueue(new Callback<Receipt>() {

                    @Override
                    public void onResponse(
                            Call<Receipt> call,
                            Response<Receipt> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            String error = "Failed to create receipt. HTTP "
                                    + response.code();

                            try {
                                if (response.errorBody() != null) {
                                    error += "\n"
                                            + response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Could not read error response", e);
                            }

                            Log.e(TAG, error);
                            callback.onError(error);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Receipt> call,
                            Throwable t
                    ) {
                        String error = t.getMessage() != null
                                ? t.getMessage()
                                : "A network error occurred.";

                        Log.e(TAG, error, t);
                        callback.onError(error);
                    }
                });
    }

    public interface ReceiptCallback {
        void onSuccess(Receipt receipt);

        void onError(String message);
    }
}
