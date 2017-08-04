package com.adyen.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adyen.core.models.paymentdetails.InputDetail;

import java.util.List;

/**
 * A custom {@link ArrayAdapter} for displaying installment options.
 */

public class InstallmentOptionsAdapter extends ArrayAdapter<InputDetail.Item> {

    @NonNull
    private final Activity context;
    @NonNull
    private final List<InputDetail.Item> installmentOptions;

    private static class ViewHolder {
        private TextView installmentOption;
    }

    public InstallmentOptionsAdapter(@NonNull Activity context, @NonNull List<InputDetail.Item> installmentOptions) {
        super(context, android.R.layout.simple_list_item_1, installmentOptions);
        this.context = context;
        this.installmentOptions = installmentOptions;
    }

    @Nullable
    @Override
    public InputDetail.Item getItem(int position) {
        return installmentOptions.get(position);
    }

    @Override
    @NonNull
    public View getView(final int position, @Nullable View view, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            viewHolder.installmentOption = (TextView) view.findViewById(android.R.id.text1);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (viewHolder != null) {
            if (viewHolder.installmentOption != null) {
                viewHolder.installmentOption.setText(installmentOptions.get(position).getName());
            }
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
