package com.example.mlsearch.ui.search;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mlsearch.R;
import com.example.mlsearch.provider.AppContract;

/**
 * Adapter for products.
 */
public class ProductsAdapter extends CursorAdapter {

    /** Constructor. */
    public ProductsAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.product_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) view.findViewById(R.id.product_item_name);
        holder.price = (TextView) view.findViewById(R.id.product_item_price);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(AppContract.Products.NAME));
        String price = cursor.getString(cursor.getColumnIndex(AppContract.Products.PRICE));

        // Get holder.
        ViewHolder holder = (ViewHolder) view.getTag();

        // Set name.
        holder.name.setText(title);
        // Set price.
        holder.price.setText(price);

        // Set background.
        int position = cursor.getPosition();
        if (position == 0) {
            view.setBackgroundResource(R.color.low_price);
        } else if (position == cursor.getCount() - 1) {
            view.setBackgroundResource(R.color.high_price);
        } else if (position % 2 != 0) {
            view.setBackgroundResource(R.color.item_odd);
        } else {
            view.setBackgroundResource(R.color.item);
        }
    }

    /** View holder. */
    private static class ViewHolder {
        TextView name;
        TextView price;
    }
}
