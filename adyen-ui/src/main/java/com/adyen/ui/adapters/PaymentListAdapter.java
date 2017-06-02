package com.adyen.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adyen.core.models.PaymentMethod;
import com.adyen.core.utils.AsyncImageDownloader;
import com.adyen.ui.R;
import com.adyen.ui.utils.IconUtil;

import java.util.List;

/**
 * A custom {@link ArrayAdapter} for displaying payment methods.
 */

public class PaymentListAdapter extends ArrayAdapter<PaymentMethod> {

    private static final String TAG = PaymentListAdapter.class.getSimpleName();

    @NonNull private final Activity context;
    @NonNull private final List<PaymentMethod> paymentMethods;
    private LayoutInflater layoutInflater;

    private static class ViewHolder {
        private TextView paymentMethodNameView;
        private ImageView imageView;
        private String url;
    }

    public PaymentListAdapter(@NonNull Activity context, @NonNull List<PaymentMethod> paymentMethods) {
        super(context, R.layout.payment_method_list, paymentMethods);

        Log.d(TAG, "PaymentListAdapter()");
        this.context = context;
        this.paymentMethods = paymentMethods;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = layoutInflater.inflate(R.layout.payment_method_list, parent, false);
            viewHolder.paymentMethodNameView = (TextView) view.findViewById(R.id.paymentMethodName);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.paymentMethodLogo);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (viewHolder != null) {
            if (viewHolder.paymentMethodNameView != null && viewHolder.imageView != null) {
                viewHolder.paymentMethodNameView.setText(paymentMethods.get(position).getName());
                Bitmap defaultImage = null;
                // TODO: Remove the following lines; the icons should be retrieved from backend.
                if ("samsungpay".equals(paymentMethods.get(position).getType())) {
                    defaultImage = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.samsung_pay_vertical_logo_artwork_rgb_0623);
                } else if ("androidpay".equals(paymentMethods.get(position).getType())) {
                    defaultImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_pay_logo);
                }
                String modifiedUrl = IconUtil.addScaleFactorToIconUrl(context,
                        paymentMethods.get(position).getLogoUrl());
                viewHolder.url = modifiedUrl;
                AsyncImageDownloader.downloadImage(getContext(), new AsyncImageDownloader.ImageListener() {
                    @Override
                    public void onImage(Bitmap bitmap, String url) {
                        if (url.equals(viewHolder.url)) {
                            viewHolder.imageView.setImageBitmap(bitmap);
                        }
                    }
                }, modifiedUrl, defaultImage);
            }
        }
        return view;
    }
}
