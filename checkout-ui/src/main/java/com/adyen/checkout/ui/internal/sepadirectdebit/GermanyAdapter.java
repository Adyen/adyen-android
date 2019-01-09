/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 03/10/2017.
 */

package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.adyen.checkout.ui.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class GermanyAdapter extends CountryAdapter {
    private static final String TAG = GermanyAdapter.class.getSimpleName();

    private static final int MIN_BANK_IDENTIFIER_SIZE = 2;
    private static final int MAX_BANK_IDENTIFIER_SIZE = 8;
    private static final int EXPECTED_NUMBER_OF_ENTRIES = 3;

    private TreeMap<String, List<String>> mBankCodeNameMapping;

    @NonNull
    @Override
    protected List<Suggestion> createSuggestions(@NonNull EditText editText, @NonNull String normalizedIban) {
        List<Suggestion> result = new ArrayList<>();

        if (mBankCodeNameMapping == null) {
            mBankCodeNameMapping = new TreeMap<>();
            loadBankCodes(editText.getContext());
        }

        if (normalizedIban.length() > COUNTRY_BLOCK_LENGTH) {
            String enteredBankCode = normalizedIban.substring(COUNTRY_BLOCK_LENGTH);
            int length = enteredBankCode.length();

            if (length > MIN_BANK_IDENTIFIER_SIZE && length < MAX_BANK_IDENTIFIER_SIZE) {

                String upperBound;
                try {
                    upperBound = String.valueOf(Integer.parseInt(enteredBankCode) + 1);
                } catch (NumberFormatException e) {
                    upperBound = null;
                }

                String fromKey = mBankCodeNameMapping.ceilingKey(enteredBankCode);
                String toKey = mBankCodeNameMapping.floorKey(upperBound);

                boolean fromInclusive = true;

                if (fromKey == null) {
                    fromKey = mBankCodeNameMapping.firstKey();
                    fromInclusive = false;
                }

                boolean toInclusive = true;

                if (toKey == null) {
                    toKey = mBankCodeNameMapping.lastKey();
                    toInclusive = false;
                }

                if (fromKey != null && toKey != null && fromKey.compareTo(toKey) <= 0) {
                    Map<String, List<String>> subMap = mBankCodeNameMapping.subMap(fromKey, fromInclusive, toKey, toInclusive);

                    for (Map.Entry<String, List<String>> entry : subMap.entrySet()) {
                        String bankCode = entry.getKey();
                        List<String> bankNames = entry.getValue();

                        for (String bankName : bankNames) {
                            String value = bankCode.substring(enteredBankCode.length());
                            Suggestion suggestion = new Suggestion(bankName, value, editText.getText().length());
                            result.add(suggestion);
                        }
                    }
                }
            }
        }

        return result;
    }

    private void loadBankCodes(@NonNull Context context) {
        WeakReference<Context> contextRef = new WeakReference<>(context);
        //noinspection unchecked
        new BankCodeLoadingTask(this).execute(contextRef);
    }

    private static final class BankCodeLoadingTask extends AsyncTask<WeakReference<Context>, Void, TreeMap<String, List<String>>> {
        private WeakReference<GermanyAdapter> mGermanyAdapterRef;

        private BankCodeLoadingTask(@NonNull GermanyAdapter germanyAdapter) {
            mGermanyAdapterRef = new WeakReference<>(germanyAdapter);
        }

        @SafeVarargs
        @Override
        protected final TreeMap<String, List<String>> doInBackground(WeakReference<Context>... params) {
            InputStream ins = getBankCodesInputStream(params[0]);
            BufferedReader reader = ins != null ? new BufferedReader(new InputStreamReader(ins, Charset.forName("UTF-8"))) : null;

            try {
                if (reader == null) {
                    return null;
                }

                Map<String, List<BankInfo>> bankInfos = new HashMap<>();

                BankInfo bankInfo;

                while ((bankInfo = BankInfo.parse(reader.readLine())) != null) {
                    String bankCode = bankInfo.mBankCode;
                    List<BankInfo> bankInfosPerBankCode = bankInfos.get(bankCode);

                    if (bankInfosPerBankCode == null) {
                        bankInfosPerBankCode = new ArrayList<>();
                        bankInfos.put(bankCode, bankInfosPerBankCode);
                    }

                    bankInfosPerBankCode.add(bankInfo);
                }

                TreeMap<String, List<String>> result = new TreeMap<>();

                for (Map.Entry<String, List<BankInfo>> entry : bankInfos.entrySet()) {
                    Set<String> names = new TreeSet<>();

                    for (BankInfo info : entry.getValue()) {
                        names.add(formatBankName(info.mBankName));
                    }

                    result.put(entry.getKey(), new ArrayList<>(names));
                }

                return result;
            } catch (IOException e) {
                Log.e(TAG, "An error occurred while trying to read the bank info.", e);

                return null;
            } finally {
                if (ins != null) {
                    try {
                        ins.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream.", e);
                    }
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing reader.", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(TreeMap<String, List<String>> result) {
            super.onPostExecute(result);

            GermanyAdapter germanyAdapter = mGermanyAdapterRef.get();

            if (germanyAdapter != null) {
                germanyAdapter.mBankCodeNameMapping = result;
            }
        }

        @Nullable
        private InputStream getBankCodesInputStream(WeakReference<Context> param) {
            Context context = param.get();

            if (context != null) {
                return context.getResources().openRawResource(R.raw.bankcodes_de);
            } else {
                return null;
            }
        }

        @NonNull
        private String formatBankName(@NonNull String bankName) {
            int splitIndex = getSplitIndex(bankName);

            if (splitIndex > 0) {
                return bankName.substring(0, splitIndex) + "\n" + bankName.substring(splitIndex + 1, bankName.length());
            } else {
                return bankName;
            }
        }

        private int getSplitIndex(@NonNull String value) {
            int length = value.length();
            int center = length / 2;

            int indexLeft = center;
            int indexRight = center + 1;

            while (true) {
                boolean searchLeft = indexLeft >= 0;
                boolean searchRight = indexRight < length;

                if (searchLeft && value.charAt(indexLeft) == ' ') {
                    return indexLeft;
                } else {
                    indexLeft -= 1;
                }

                if (searchRight && value.charAt(indexRight) == ' ') {
                    return indexRight;
                } else {
                    indexRight += 1;
                }

                if (!searchLeft && !searchRight) {
                    break;
                }
            }

            return -1;
        }
    }

    private static final class BankInfo {
        private final String mBankCode;

        private final String mBankName;

        private final String mBranchName;

        @Nullable
        private static BankInfo parse(@Nullable String line) {
            if (line == null) {
                return null;
            }

            String[] entries = line.split(";");

            if (entries.length != EXPECTED_NUMBER_OF_ENTRIES) {
                return null;
            }

            return new BankInfo(entries[0], entries[1], entries[2]);
        }

        private BankInfo(@NonNull String bankCode, @NonNull String bankName, @NonNull String branchName) {
            mBankCode = bankCode;
            mBankName = bankName;
            mBranchName = branchName;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) %s", mBankName, mBranchName, mBankCode);
        }
    }
}
