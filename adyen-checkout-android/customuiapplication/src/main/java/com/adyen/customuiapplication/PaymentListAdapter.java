package com.adyen.customuiapplication;

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

import com.adyen.core.models.PaymentMethod;
import com.adyen.core.utils.AsyncImageDownloader;

import java.util.List;

/**
 * A custom {@link ArrayAdapter} for displaying payment methods.
 */

class PaymentListAdapter extends ArrayAdapter<PaymentMethod> {

    private static final String TAG = PaymentListAdapter.class.getSimpleName();

    @NonNull private final Activity context;
    @NonNull private final List<PaymentMethod> paymentMethods;
    private LayoutInflater layoutInflater;

    private static class ViewHolder {
        private TextView paymentMethodNameView;
        private ImageView imageView;
    }

    PaymentListAdapter(@NonNull Activity context, @NonNull List<PaymentMethod> paymentMethods) {
        super(context, R.layout.payment_method_list, paymentMethods);
        Log.d(TAG, "PaymentListAdapter()");

        this.context = context;
        this.paymentMethods = paymentMethods;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
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
                AsyncImageDownloader.downloadImage(context, viewHolder.imageView,
                        paymentMethods.get(position).getLogoUrl(), defaultImage);
            }
        }
        return view;

    }

}
