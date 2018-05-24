package com.adyen.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;
import com.adyen.ui.adapters.IssuerListAdapter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Fragment for isser selection for PaymentMethod ideal.
 * Should be instantiated via {@link IssuerSelectionFragmentBuilder}.
 */
public class IssuerSelectionFragment extends Fragment {

    private static final String TAG = IssuerSelectionFragment.class.getSimpleName();
    private IssuerSelectionListener issuerSelectionListener;
    private List<InputDetail.Item> issuers = new CopyOnWriteArrayList<>();
    private PaymentMethod paymentMethod;

    private int theme;

    /**
     * Use {@link IssuerSelectionFragmentBuilder} instead.
     */
    public IssuerSelectionFragment() {
        //Default empty constructor
    }

    /**
     * The listener interface for receiving selected issuer.
     */
    public interface IssuerSelectionListener {
        void onIssuerSelected(String issuer);
    }


    @Override
    public void setArguments(final Bundle args) {
        super.setArguments(args);
        paymentMethod = (PaymentMethod) args.get(CheckoutActivity.PAYMENT_METHOD);

        theme = args.getInt("theme");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        final View fragmentView;
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        fragmentView = localInflater.inflate(R.layout.issuer_selection_fragment, container, false);

        for (InputDetail inputDetail : paymentMethod.getInputDetails()) {
            if (inputDetail.getKey().equals("idealIssuer") || inputDetail.getKey().equals("issuer")) {
                issuers = inputDetail.getItems();
                break;
            }
        }

        final IssuerListAdapter issuerListAdapter = new IssuerListAdapter(getActivity(), issuers);
        final ListView listView = (ListView) fragmentView.findViewById(R.id.issuer_methods_list);
        listView.setAdapter(issuerListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                String selectedIssuer = issuers.get(i).getId();
                issuerSelectionListener.onIssuerSelected(selectedIssuer);
            }
        });

        if (getActivity() instanceof CheckoutActivity) {
            ((CheckoutActivity) getActivity()).setActionBarTitle(paymentMethod.getName());
        }
        return fragmentView;
    }

    void setIssuerSelectionListener(IssuerSelectionListener listener) {
        this.issuerSelectionListener = listener;
    }
}
