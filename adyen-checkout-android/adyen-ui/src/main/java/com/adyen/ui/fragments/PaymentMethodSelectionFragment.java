package com.adyen.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;
import com.adyen.ui.adapters.PaymentListAdapter;

import java.util.ArrayList;

import static com.adyen.ui.activities.CheckoutActivity.PAYMENT_METHODS;
import static com.adyen.ui.activities.CheckoutActivity.PREFERED_PAYMENT_METHODS;

/**
 * Fragment for displaying payment methods.
 * Should be instantiated via {@link PaymentMethodSelectionFragmentBuilder}.
 */
public class PaymentMethodSelectionFragment extends Fragment {

    private static final String TAG = PaymentMethodSelectionFragment.class.getSimpleName();
    private PaymentMethodSelectionListener paymentMethodSelectionListener;
    private ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();
    private ArrayList<PaymentMethod> preferredPaymentMethods = new ArrayList<>();

    private View preferredPaymentMethodsLayout;

    private int theme;

    /**
     * Use {@link PaymentMethodSelectionFragmentBuilder} instead.
     */
    public PaymentMethodSelectionFragment() {
        //Default empty constructor
    }

    /**
     * The listener interface for receiving payment method selection result.
     * Container Activity must implement this interface.
     */
    public interface PaymentMethodSelectionListener {
        void onPaymentMethodSelected(PaymentMethod paymentMethod);
    }

    void setPaymentMethodSelectionListener(PaymentMethodSelectionListener listener) {
        this.paymentMethodSelectionListener = listener;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        theme = args.getInt("theme");

        paymentMethods = (ArrayList<PaymentMethod>) args.getSerializable(PAYMENT_METHODS);
        if (args.getSerializable(PREFERED_PAYMENT_METHODS) != null) {
            preferredPaymentMethods = (ArrayList<PaymentMethod>) args.getSerializable(PREFERED_PAYMENT_METHODS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        final View fragmentView;

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        fragmentView = localInflater.inflate(R.layout.payment_method_selection_fragment, container, false);

        final PaymentListAdapter paymentListAdapter = new PaymentListAdapter(
                getActivity(), paymentMethods);
        final ListView listView = (ListView) fragmentView.findViewById(android.R.id.list);
        listView.setAdapter(paymentListAdapter);

        final PaymentListAdapter adyenUIPreferredPaymentListAdapter = new PaymentListAdapter(
                getActivity(), preferredPaymentMethods);
        final ListView preferredListView = (ListView) fragmentView.findViewById(R.id.preferred_payment_methods_list);
        preferredListView.setAdapter(adyenUIPreferredPaymentListAdapter);

        listView.setOnItemClickListener(new OnPaymentMethodClick());
        preferredListView.setOnItemClickListener(new OnPreferredPaymentMethodClick());

        ViewTreeObserver listVTO = listView.getViewTreeObserver();
        listVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                resizeListView(listView, true);
            }
        });

        ViewTreeObserver preferredListVTO = preferredListView.getViewTreeObserver();
        preferredListVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                preferredListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                resizeListView(preferredListView, false);
            }
        });

        preferredPaymentMethodsLayout = fragmentView.findViewById(R.id.preferred_payment_methods_layout);

        /*
        paymentMethods.clear();
        preferredPaymentMethods.clear();
        preferredPaymentMethods.addAll(((CheckoutActivity) getActivity()).getPreferredPaymentMethods());
        paymentMethods.addAll(((CheckoutActivity) getActivity()).getAvailablePaymentMethods());
        adyenUIPreferredPaymentListAdapter.notifyDataSetChanged();
        paymentListAdapter.notifyDataSetChanged();
        */

        if (preferredPaymentMethods.isEmpty()) {
            preferredPaymentMethodsLayout.setVisibility(View.GONE);
        } else {
            preferredPaymentMethodsLayout.setVisibility(View.VISIBLE);
        }

        if (getActivity() instanceof CheckoutActivity) {
            ((CheckoutActivity) getActivity()).setActionBarTitle(R.string.paymentMethods_title);
        }

        return fragmentView;
    }

    private void resizeListView(ListView listView, boolean isBottomList) {
        ListAdapter adapter = listView.getAdapter();
        int count = adapter.getCount();
        int itemsHeight = 0;
        // Your views have the same layout, so all of them have
        // the same height
        View oneChild = listView.getChildAt(0);
        if (oneChild == null) {
            return;
        }
        itemsHeight = oneChild.getHeight();
        // Resize your list view
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
        params.height = isBottomList ? itemsHeight * count + itemsHeight / 2 : itemsHeight * count;
        listView.setLayoutParams(params);
    }

    private void handleOnPaymentMethodClick(int item, boolean isPreferredPaymentMethod) {
        final PaymentMethod selectedPaymentMethod = isPreferredPaymentMethod ? preferredPaymentMethods.get(item) : paymentMethods.get(item);
        paymentMethodSelectionListener.onPaymentMethodSelected(selectedPaymentMethod);
    }

    public class OnPaymentMethodClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            handleOnPaymentMethodClick(position, false);
        }
    }

    public class OnPreferredPaymentMethodClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            handleOnPaymentMethodClick(position, true);
        }
    }
}
