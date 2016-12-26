package com.example.mlsearch.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.mlsearch.provider.AppContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * This service fetch products from search service.
 */
public class ApiIntentService extends IntentService {
    /** Log tag. */
    private static final String TAG = ApiIntentService.class.getSimpleName();

    /** Action for result intent. */
    public static final String ACTION_SERVICE_FINISHED = "ACTION_SERVICE_FINISHED";

    /** Query extra. */
    public static final String EXTRA_QUERY = "EXTRA_QUERY";

    /** Result extra. */
    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    /** Indicates that service finished successfully. */
    public static final int RESULT_OK = 1;
    /** Indicates that a connection error has happened. */
    public static final int RESULT_NETWORK_FAIL = 2;
    /** Indicates that a application error has happened. */
    public static final int RESULT_APP_FAIL = 3;
    /** Indicates that a server error has happened. */
    public static final int RESULT_SERVICE_FAIL = 4;

    /** Base url. */
    private static final String BASE_URL = "https://api.mercadolibre.com";
    /** Search service uri. */
    private static final String SEARCH_URI = "sites/MLA/search";
    /** Query parameter. */
    private static final String PARAM_QUERY = "q";

    /** Response parser. */
    private ApiResponseParser mParser;

    /** Constructor. */
    public ApiIntentService() {
        super(ApiIntentService.class.getSimpleName());
        mParser = new ApiResponseParser();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle result = new Bundle();

        // Get the query extra.
        String query = intent.getStringExtra(EXTRA_QUERY);
        if (TextUtils.isEmpty(query)) {
            // Invalid query.
            Log.e(TAG, "Invalid query.");
            result.putInt(EXTRA_RESULT, RESULT_APP_FAIL);
        } else {
            InputStream in = null;
            try {
                Uri uri = Uri.parse(BASE_URL).buildUpon().appendEncodedPath(SEARCH_URI)
                        .appendQueryParameter(PARAM_QUERY, query).build();
                // Setup connection.
                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                // Do request.
                connection.connect();
                int response = connection.getResponseCode();

                // Check response.
                Log.d(TAG, "The response in: " + response);
                if (response == HttpsURLConnection.HTTP_OK) {
                    in = connection.getInputStream();
                    ArrayList<ContentProviderOperation> operations = new ArrayList<>();

                    // Remove previous data.
                    operations.add(ContentProviderOperation.newDelete(
                            AppContract.Products.buildUri()).build());

                    // Parse response.
                    mParser.parse(in, operations);

                    // Execute operations.
                    getContentResolver().applyBatch(AppContract.CONTENT_AUTHORITY,
                            operations);

                    result.putInt(EXTRA_RESULT, RESULT_OK);
                } else {
                    result.putInt(EXTRA_RESULT, RESULT_SERVICE_FAIL);
                }
            } catch (IOException e) {
                Log.e(TAG, "Connection error", e);
                result.putInt(EXTRA_RESULT, RESULT_NETWORK_FAIL);
            } catch (Exception e) {
                Log.e(TAG, "App error", e);
                result.putInt(EXTRA_RESULT, RESULT_APP_FAIL);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // Notify results.
        Intent resultIntent = new Intent(ACTION_SERVICE_FINISHED);
        resultIntent.putExtras(result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
