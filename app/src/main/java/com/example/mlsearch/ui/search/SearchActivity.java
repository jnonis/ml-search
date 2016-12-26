package com.example.mlsearch.ui.search;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.mlsearch.R;

/**
 * This activity shows a input field to search products and a list of results.
 */
public class SearchActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    if (savedInstanceState == null) {
      FragmentTransaction transaction = getSupportFragmentManager()
          .beginTransaction();
      transaction.replace(R.id.container, SearchFragment.newInstance());
      transaction.commit();
    }
  }
}
