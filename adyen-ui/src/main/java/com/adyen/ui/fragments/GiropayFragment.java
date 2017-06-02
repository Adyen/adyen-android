package com.adyen.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.adyen.core.internals.HttpClient;
import com.adyen.core.models.Amount;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;
import com.adyen.ui.views.GiroPayEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Fragment for issuing bank selection for PaymentMethod Giropay.
 */
public final class GiropayFragment extends Fragment {

    //TODO get proper url
    private static final String SEARCH_ENDPOINT = "https://live.adyen.com/hpp/getGiroPayBankList.shtml?searchStr=";
    private static final int MIN_LENGTH_TO_LOOKUP = 3;

    private AdyenUIGiroPayIssuersListAdapter issuerListAdapter;
    private ListView listView;
    private View loadingView;
    private GiroPayEditText editText;
    private Button payButton;
    private View exampleView;

    private GiroPayLookUpASyncTask lookupAsyncTask = null;

    private Amount amount;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        amount = (Amount) args.get(CheckoutActivity.AMOUNT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.giropay_fragment, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        issuerListAdapter = new AdyenUIGiroPayIssuersListAdapter(getActivity());
        listView = (ListView) fragmentView.findViewById(R.id.giropay_list_view);
        listView.setAdapter(issuerListAdapter);

        editText = (GiroPayEditText) fragmentView.findViewById(R.id.adyen_giropay_lookup_edit_text);
        editText.addTextChangedListener(giroPayTextWatcher);

        TextView amountTextView = (TextView) fragmentView.findViewById(R.id.amount_text_view);
        final String amountLocalized = AmountUtil.format(amount, true, StringUtils.getLocale(getActivity()));
        final String amountString = getString(R.string.pay_with_amount, amountLocalized);
        amountTextView.setText(amountString);

        loadingView = fragmentView.findViewById(R.id.loading_icon_view);
        payButton = (Button) fragmentView.findViewById(R.id.pay_giropay_button);
        exampleView = fragmentView.findViewById(R.id.giropay_example_layout);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void selectedBank(final GiroPayIssuer bank) {
        editText.removeTextChangedListener(giroPayTextWatcher);
        editText.setText(bank.getBankName());
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        editText.setSingleLine(false);
        editText.clearFocus();
        editText.setEnabled(false);
        editText.setCancelDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.clear_icon),
                new GiroPayEditText.OnDrawableClickListener() {
            @Override
            public void onDrawableClick() {
                unSelectedBank();
            }
        });

        issuerListAdapter.clearData();
        issuerListAdapter.notifyDataSetChanged();

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        payButton.setEnabled(true);
    }

    private void unSelectedBank() {
        editText.setEnabled(true);
        editText.setText("");
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editText.setSingleLine(true);
        editText.addTextChangedListener(giroPayTextWatcher);
        editText.requestFocus();
        editText.setCompoundDrawables(null, null, null, null);

        payButton.setEnabled(false);

        exampleView.setVisibility(View.VISIBLE);
    }

    private TextWatcher giroPayTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String str = s.toString();
            if (str.length() >= MIN_LENGTH_TO_LOOKUP) {
                exampleView.setVisibility(View.GONE);
                if (issuerListAdapter.getFullCount() == 0) {
                    if (lookupAsyncTask != null) {
                        lookupAsyncTask.cancel(true);
                    }
                    lookupAsyncTask = new GiroPayLookUpASyncTask(str);
                    lookupAsyncTask.execute();
                } else {
                    issuerListAdapter.filter(str);
                    issuerListAdapter.notifyDataSetChanged();
                }
            } else {
                exampleView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                if (lookupAsyncTask != null) {
                    lookupAsyncTask.cancel(true);
                }
                issuerListAdapter.clearData();
                issuerListAdapter.notifyDataSetChanged();
            }
        }
    };

    private final class GiroPayLookUpASyncTask extends AsyncTask<Void, Void, String> {

        private String searchStr = "";

        private GiroPayLookUpASyncTask(@NonNull final String searchString) {
            this.searchStr = searchString;
        }

        @Override
        protected void onPreExecute() {
            if (issuerListAdapter.getFullCount() == 0) {
                loadingView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return new String(new HttpClient<>().get(SEARCH_ENDPOINT + searchStr.substring(
                        0, MIN_LENGTH_TO_LOOKUP), null), Charset.forName("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String response) {
            if (loadingView.getVisibility() == View.VISIBLE) {
                loadingView.setVisibility(View.GONE);
            }
            try {
                issuerListAdapter.setData(new JSONArray(response), searchStr.substring(0, MIN_LENGTH_TO_LOOKUP));
                issuerListAdapter.filter(searchStr);
                issuerListAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            if (loadingView.getVisibility() == View.VISIBLE) {
                loadingView.setVisibility(View.GONE);
            }
        }
    }

    private final class AdyenUIGiroPayIssuersListAdapter extends BaseAdapter {

        private String searchStr = "";

        private ArrayList<GiroPayIssuer> giroPayIssuers = new ArrayList<>();
        private ArrayList<GiroPayIssuer> filteredIssuers = new ArrayList<>();

        private LayoutInflater layoutInflater;

        private AdyenUIGiroPayIssuersListAdapter(Context context) {
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return filteredIssuers.size();
        }

        private int getFullCount() {
            return giroPayIssuers.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredIssuers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = layoutInflater.inflate(R.layout.giropay_list_item, parent, false);

            final GiroPayIssuer current = filteredIssuers.get(position);
            final String bankName = current.getBankName();

            TextView nameTextView = (TextView) view.findViewById(R.id.name_giropay_issuer);

            int startIndex = bankName.toLowerCase().indexOf(searchStr.toLowerCase());
            if (startIndex != -1) {
                int endIndex = startIndex + searchStr.length();
                final SpannableStringBuilder ssb = new SpannableStringBuilder(bankName);
                ssb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), startIndex,
                        endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                nameTextView.setText(ssb);
            } else {
                nameTextView.setText(bankName);
            }

            nameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedBank(current);
                }
            });
            return view;
        }

        private void setData(JSONArray issuersJson, String searchStr) throws JSONException {
            clearData();
            for (int i = 0; i < issuersJson.length(); i++) {
                this.giroPayIssuers.add(new GiroPayIssuer(issuersJson.getJSONObject(i)));
            }
            filter(searchStr);
        }

        private void clearData() {
            this.giroPayIssuers.clear();
            this.filteredIssuers.clear();
            this.searchStr = "";
        }

        private void filter(String newSearchStr) {
            ArrayList<GiroPayIssuer> toFilterFrom;
            if (filteredIssuers.isEmpty() || searchStr.length() > newSearchStr.length()) {
                toFilterFrom = giroPayIssuers;
            } else {
                toFilterFrom = filteredIssuers;
            }
            ArrayList<GiroPayIssuer> results = new ArrayList();
            for (GiroPayIssuer issuer : toFilterFrom) {
                if (issuer.getBankName().contains(newSearchStr)) {
                    results.add(issuer);
                }
            }
            this.searchStr = newSearchStr;
            filteredIssuers = results;
        }
    }

    private static final class GiroPayIssuer {
        private String bankName;
//        private String bic;
//        private String blz;

        private GiroPayIssuer(JSONObject jsonObject) throws JSONException {
            this.bankName = jsonObject.getString("bankName");
//            this.bic = jsonObject.getString("bic");
//            this.blz = jsonObject.getString("blz");
        }

        private String getBankName() {
            return bankName;
        }
    }
}
