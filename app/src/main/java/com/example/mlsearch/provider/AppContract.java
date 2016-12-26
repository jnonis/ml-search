package com.example.mlsearch.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract to communicate with {@link AppContentProvider}
 */
public class AppContract {
    /** The authority for app contents. */
    public static final String CONTENT_AUTHORITY = "com.example.mlsearch.provider";
    /** Base URI to access provider's content. */
    protected static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /** Base content type. */
    protected static final String BASE_CONTENT_TYPE = "vnd.mlsearch.app.dir/vnd.products.";
    /** Base item Content type. */
    protected static final String BASE_CONTENT_ITEM_TYPE = "vnd.mlsearch.app.item/vnd.products.";


    /** Product columns. */
    interface ProductsColumns {
        /** Product id. */
        String ID = "id";
        /** Payment name. */
        String NAME = "name";
        /** Product price. */
        String PRICE = "price";
    }

    /** Product contract. */
    public static class Products implements ProductsColumns, BaseColumns {

        /** Uri Path. */
        static final String PATH = "product";

        /** Content Uri. */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /** Content type. */
        public static final String CONTENT_TYPE = BASE_CONTENT_TYPE + PATH;

        /** Item Content type. */
        public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE + PATH;

        /** Default projection. */
        public static final String[] DEFAULT_PROJECTION = new String[]{
                _ID, ID, NAME, PRICE};

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = PRICE + " ASC";

        /** Build {@link Uri} for request all entities. */
        public static Uri buildUri() {
            return CONTENT_URI.buildUpon().build();
        }

        /** Build {@link Uri} for requested entity. */
        public static Uri buildUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        /** Extract the id from given {@link Uri} */
        public static final String getId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
