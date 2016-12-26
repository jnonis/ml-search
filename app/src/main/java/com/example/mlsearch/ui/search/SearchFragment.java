package com.example.mlsearch.ui.search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.mlsearch.R;
import com.example.mlsearch.provider.AppContract;
import com.example.mlsearch.service.ApiIntentService;
import com.example.mlsearch.ui.widget.ErrorDialogFragment;


/**
 * This fragment shows a input field to search products and a list of results.
 */
public class SearchFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    /** Error dialog tag. */
    private static final String ERROR_DIALOG_FRAGMENT = "ERROR_DIALOG_FRAGMENT";
    /** Search input layout. */
    private TextInputLayout mSearchInputLayoutView;
    /** Search input view. */
    private TextInputEditText mSearchInputView;
    /** Search button view. */
    private Button mSearchButtonView;
    /** List view of payment methods. */
    private ListView mPaymentsView;
    /** List adapter. */
    private ProductsAdapter mAdapter;
    /** Service result receiver. */
    private BroadcastReceiver mResultReceiver;

    /** Create a instance of the fragment, */
    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep the fragment alive on configuration changed.
        setRetainInstance(true);

        mResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = intent.getExtras().getInt(ApiIntentService.EXTRA_RESULT);
                handleServiceResult(result);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchInputLayoutView = (TextInputLayout) view.findViewById(R.id.search_input_layout);
        mSearchInputView = (TextInputEditText) view.findViewById(R.id.search_input);
        mSearchButtonView = (Button) view.findViewById(R.id.search_button);
        mPaymentsView = (ListView) view.findViewById(android.R.id.list);
        View emptyView = view.findViewById(android.R.id.empty);
        mPaymentsView.setEmptyView(emptyView);

        mSearchButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearch();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup adapter.
        if (mAdapter == null) {
            // Use application context to avoid leaks.
            mAdapter = new ProductsAdapter(getActivity().getApplicationContext());
        }
        mPaymentsView.setAdapter(mAdapter);

        // Initialize loader.
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mResultReceiver,
                new IntentFilter(ApiIntentService.ACTION_SERVICE_FINISHED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mResultReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPaymentsView.setAdapter(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), AppContract.Products.buildUri(),
                AppContract.Products.DEFAULT_PROJECTION, null, null,
                AppContract.Products.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

    /** Handles service results. */
    private void handleServiceResult(int result) {
        switch (result) {
            case ApiIntentService.RESULT_OK:
                break;
            case ApiIntentService.RESULT_NETWORK_FAIL:
                showErrorDialog(R.string.error_connection);
                break;
            case ApiIntentService.RESULT_APP_FAIL:
                showErrorDialog(R.string.error_app);
                break;
            case ApiIntentService.RESULT_SERVICE_FAIL:
                showErrorDialog(R.string.error_service);
                break;
        }
    }

    /** Called when user tap con search button. */
    private void onSearch() {
        String query = mSearchInputView.getText().toString();

        // Validate query.
        if (validate(query)) {
            // Start request.
            Intent intent = new Intent(getActivity(), ApiIntentService.class);
            intent.putExtra(ApiIntentService.EXTRA_QUERY, query);
            getActivity().startService(intent);
        }
    }

    /**
     * Validate given query.
     * @param query the query.
     * @return true if it is valid.
     */
    private boolean validate(String query) {
        if (TextUtils.isEmpty(query)) {
            mSearchInputLayoutView.setError(getString(R.string.error_invalid_query));
            return false;
        } else {
            mSearchInputLayoutView.setError(null);
        }
        return true;
    }

    /**
     * Shows an error dialog.
     * It will remove any other previous error dialog.
     * @param resErrorMessage the resource of error messages.
     */
    private void showErrorDialog(int resErrorMessage) {
        ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(
                resErrorMessage);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Remove old fragment.
        Fragment dialog = fragmentManager.findFragmentByTag(
                ERROR_DIALOG_FRAGMENT);
        if (dialog != null) {
            transaction.remove(dialog);
        }

        // Add new fragment.
        transaction.add(errorDialog, ERROR_DIALOG_FRAGMENT);
        transaction.commit();
    }
}
