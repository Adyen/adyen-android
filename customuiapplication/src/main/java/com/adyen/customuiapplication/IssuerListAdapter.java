package com.adyen.customuiapplication;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adyen.core.models.Issuer;
import com.adyen.core.utils.AsyncImageDownloader;

import java.util.List;

/**
 * A custom {@link ArrayAdapter} for displaying issuers.
 */

class IssuerListAdapter extends ArrayAdapter<Issuer> {

    private static final String TAG = IssuerListAdapter.class.getSimpleName();

    @NonNull
    private final Activity context;
    @NonNull
    private final List<Issuer> issuers;

    private static class ViewHolder {
        private TextView paymentMethodNameView;
        private ImageView imageView;
    }

    IssuerListAdapter(@NonNull Activity context, @NonNull List<Issuer> issuers) {
        super(context, R.layout.payment_method_list, issuers);
        Log.d(TAG, "IssuerListAdapter()");

        this.context = context;
        this.issuers = issuers;
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.payment_method_list, parent, false);

            viewHolder.paymentMethodNameView = (TextView) view.findViewById(R.id.paymentMethodName);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.paymentMethodLogo);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.paymentMethodNameView.setText(issuers.get(position).getIssuerName());
        AsyncImageDownloader.downloadImage(context, viewHolder.imageView, issuers.get(position).getIssuerLogoUrl(),
                null);

        return view;
    }
}
