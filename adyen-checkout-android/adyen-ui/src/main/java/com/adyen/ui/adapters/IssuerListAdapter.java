package com.adyen.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.utils.AsyncImageDownloader;
import com.adyen.ui.R;
import com.adyen.ui.utils.IconUtil;

import java.util.List;

/**
 * A custom {@link ArrayAdapter} for displaying issuers.
 */

public class IssuerListAdapter extends ArrayAdapter<InputDetail.Item> {

    private static final String TAG = IssuerListAdapter.class.getSimpleName();

    @NonNull
    private final Activity context;
    @NonNull
    private final List<InputDetail.Item> issuers;

    private static class ViewHolder {
        private TextView paymentMethodNameView;
        private ImageView imageView;
        private String url;
    }

    public IssuerListAdapter(@NonNull Activity context, @NonNull List<InputDetail.Item> issuers) {
        super(context, R.layout.payment_method_list, issuers);
        Log.d(TAG, "IssuerListAdapter()");
        this.context = context;
        this.issuers = issuers;
    }

    @Nullable
    @Override
    public InputDetail.Item getItem(int position) {
        return issuers.get(position);
    }

    @Override
    @NonNull
    public View getView(final int position, @Nullable View view, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
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

        if (viewHolder != null) {
            if (viewHolder.paymentMethodNameView != null && viewHolder.imageView != null) {
                viewHolder.paymentMethodNameView.setText(issuers.get(position).getName());
                String modifiedUrl = IconUtil.addScaleFactorToIconUrl(context,
                        issuers.get(position).getImageUrl());
                viewHolder.url = modifiedUrl;
                AsyncImageDownloader.downloadImage(getContext(), new AsyncImageDownloader.ImageListener() {
                    @Override
                    public void onImage(Bitmap bitmap, String url) {
                        if (url.equals(viewHolder.url)) {
                            viewHolder.imageView.setImageBitmap(bitmap);
                        }
                    }
                }, modifiedUrl, null);
            }
        }
        return view;
    }
}
