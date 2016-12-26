package com.example.mlsearch.service;

import android.content.ContentProviderOperation;

import com.example.mlsearch.provider.AppContract;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Parse the json content from a input stream and create the {@link
 * ContentProviderOperation} for the results.
 */
public class ApiResponseParser {
    /** Max count of produtcs to store. */
    private static final int MAX_COUNT = 5;
    /** Results json attribute. */
    private static final String RESULTS = "results";
    /** Id json attribute. */
    private static final String ID = "id";
    /** Title json attribute. */
    private static final String TITLE = "title";
    /** Price json attribute. */
    private static final String PRICE = "price";

    /**
     * Parse the json content from input stream and returns the content provider
     * operations for parsed payments.
     * @param in the input stream.
     * @param operations operation list.
     * @throws IOException in case of connection error.
     */
    public void parse(InputStream in,
            ArrayList<ContentProviderOperation> operations)
            throws IOException {
        // Add a delete operation for previous results.
        operations.add(ContentProviderOperation.newDelete(AppContract.Products.buildUri()).build());

        JsonReader reader = new JsonReader(new InputStreamReader(in));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case RESULTS:
                    int count = 0;
                    reader.beginArray();
                    while (reader.hasNext()) {
                        if (count < MAX_COUNT) {
                            parseProduct(reader, operations);
                        } else {
                            reader.skipValue();
                        }
                        count++;
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    /**
     * Parse a json product from input stream and adds the content provider operations.
     * @param reader the reader.
     * @param operations operation list.
     * @throws IOException in case of connection error.
     */
    private void parseProduct(JsonReader reader, ArrayList<ContentProviderOperation> operations)
            throws IOException {
        // Create a insert.
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(AppContract.Products.buildUri());

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case ID:
                    builder.withValue(AppContract.Products.ID, nextStringSafe(reader));
                    break;
                case TITLE:
                    builder.withValue(AppContract.Products.NAME, nextStringSafe(reader));
                    break;
                case PRICE:
                    builder.withValue(AppContract.Products.PRICE, nextStringSafe(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        // Add the insert.
        operations.add(builder.build());
    }

    /**
     * Utility to read an string safely in case of null content.
     * @param reader reader with the content to onParseResponse.
     * @return a string or null case of null content.
     * @throws IOException in case of error reading from stream.
     */
    private String nextStringSafe(final JsonReader reader) throws
            IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return reader.nextString();
    }
}
