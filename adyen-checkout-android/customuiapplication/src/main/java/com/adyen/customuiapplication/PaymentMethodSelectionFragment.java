package com.adyen.customuiapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adyen.core.models.PaymentMethod;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fragment for displaying payment methods.
 */
public class PaymentMethodSelectionFragment extends Fragment {

    private static final String TAG = PaymentMethodSelectionFragment.class.getSimpleName();
    private PaymentMethodSelectionListener paymentMethodSelectionListener;
    private final List<PaymentMethod> paymentMethods = new CopyOnWriteArrayList<>();

    /**
     * The listener interface for receiving payment method selection result.
     * Container Activity must implement this interface.
     */
    public interface PaymentMethodSelectionListener {
        void onPaymentMethodSelected(PaymentMethod paymentMethod);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            this.paymentMethodSelectionListener = (PaymentMethodSelectionListener) context;
        } catch (final ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " is not a PaymentMethodSelectionListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.payment_method_selection_fragment, container, false);
        final PaymentListAdapter paymentListAdapter = new PaymentListAdapter(getActivity(), paymentMethods);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(paymentListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                final PaymentMethod selected = paymentMethods.get(i);
                paymentMethodSelectionListener.onPaymentMethodSelected(selected);

            }
        });

        paymentMethods.clear();
        paymentMethods.addAll(((MainActivity) getActivity()).getPreferredPaymentMethods());
        paymentMethods.addAll(((MainActivity) getActivity()).getAvailablePaymentMethods());
        paymentListAdapter.notifyDataSetChanged();

        // Inflate the layout for this fragment
        return view;
    }

}
