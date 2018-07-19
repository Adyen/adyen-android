package com.adyen.checkout.nfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.card.Card;
import com.adyen.checkout.core.card.CardValidator;
import com.adyen.checkout.nfc.internal.ApplicationFileLocator;
import com.adyen.checkout.nfc.internal.ByteUtil;
import com.adyen.checkout.nfc.internal.CardScheme;
import com.adyen.checkout.nfc.internal.Command;
import com.adyen.checkout.nfc.internal.Parser;
import com.adyen.checkout.nfc.internal.PdolDataGenerator;
import com.adyen.checkout.nfc.internal.Response;
import com.adyen.checkout.nfc.internal.TagLengthValue;
import com.adyen.checkout.nfc.internal.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The {@link NfcCardReader} provides an interface to read {@link Card} information from cards with an NFC chip.
 * <p>
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 29/08/2017.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public final class NfcCardReader {
    private static final byte[] PSE = "1PAY.SYS.DDF01".getBytes(ByteUtil.NFC_CHARSET);

    private static final byte[] PPSE = "2PAY.SYS.DDF01".getBytes(ByteUtil.NFC_CHARSET);

    private final Activity mActivity;

    private final NfcAdapter mNfcAdapter;

    private final Listener mListener;

    /**
     * Create a new {@link NfcCardReader}.
     *
     * @param activity The hosting {@link Activity}.
     * @param listener The {@link Listener} used to receive callbacks.
     * @return An {@link NfcCardReader} or {@code null}, if the device does not have an {@link NfcAdapter}.
     */
    @Nullable
    public static NfcCardReader getInstance(@NonNull Activity activity, @NonNull Listener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

            if (nfcAdapter != null) {
                return new NfcCardReader(activity, nfcAdapter, listener);
            }
        }

        return null;
    }

    private NfcCardReader(@NonNull Activity activity, @NonNull NfcAdapter nfcAdapter, @NonNull Listener listener) {
        mActivity = activity;
        mNfcAdapter = nfcAdapter;
        mListener = listener;
    }

    /**
     * @return Whether the device has NFC enabled in the settings.
     */
    public boolean isNfcEnabledOnDevice() {
        return mNfcAdapter.isEnabled();
    }

    /**
     * Enable the reader. Should be called in {@link Activity#onResume()}.
     */
    public void enableWithSounds(boolean playSounds) {
        int flags = NfcAdapter.FLAG_READER_NFC_A
                | NfcAdapter.FLAG_READER_NFC_B
                | (playSounds ? 0 : NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS);

        try {
            mNfcAdapter.enableReaderMode(mActivity, new NfcReaderCallback(), flags, null);
        } catch (UnsupportedOperationException e) {
            notifyError(Error.UNAVAILABLE);
        }
    }

    /**
     * Disable the reader. Should be called in {@link Activity#onPause()}.
     */
    public void disable() {
        mNfcAdapter.disableReaderMode(mActivity);
    }

    private void notifyChipDiscovered(final boolean supported) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onChipDiscovered(supported);
            }
        });
    }

    private void notifyCardDiscovered() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onCardDiscovered();
            }
        });
    }

    private void notifyCardRead(@NonNull final Card card) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onCardRead(card);
            }
        });
    }

    private void notifyError(@NonNull final Error error) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onError(error);
            }
        });
    }

    private boolean checkNotifyListenerRead(@NonNull byte[] trackTwoEquivalentDataValue) {
        String dataElements = ByteUtil.bytesToHex(trackTwoEquivalentDataValue).replaceAll("\\s", "");
        String[] panAndRest = dataElements.split("D");

        if (panAndRest.length == 2) {
            String pan = panAndRest[0];
            String rest = panAndRest[1];

            if (rest.length() >= 4) {
                try {
                    int expiryMonth = Integer.parseInt(rest.substring(2, 4));
                    int expiryYear = Integer.parseInt(rest.substring(0, 2));

                    Card card = new Card.Builder()
                            .setNumber(pan)
                            .setExpiryDate(expiryMonth, expiryYear)
                            .build();

                    notifyCardRead(card);

                    return true;
                } catch (NumberFormatException e) {
                    // Ignore.
                }
            }
        }

        return false;
    }

    private boolean checkNotifyListenerRead(@NonNull List<TagLengthValue> recordInformations) {
        for (TagLengthValue recordInformation : recordInformations) {
            TagLengthValue panTagLengthValue = recordInformation.find(Tags.APPLICATION_PRIMARY_ACCOUNT_NUMBER);
            TagLengthValue expiryDateTagLengthValue = recordInformation.find(Tags.APPLICATION_EXPIRATION_DATE);

            byte[] panBytes = panTagLengthValue != null ? panTagLengthValue.getValue() : null;
            byte[] expiryDateBytes = expiryDateTagLengthValue != null ? expiryDateTagLengthValue.getValue() : null;

            if (panBytes != null && expiryDateBytes != null) {
                String pan = ByteUtil.bytesToHex(panBytes).replaceAll("\\s", "");

                if (pan.length() > CardValidator.NUMBER_MAXIMUM_LENGTH) {
                    pan = pan.substring(0, CardValidator.NUMBER_MAXIMUM_LENGTH);
                }

                try {
                    String expiryDateString = ByteUtil.bytesToHex(expiryDateBytes).replaceAll("\\s", "");
                    int expiryMonth = Integer.parseInt(expiryDateString.substring(2, 4));
                    int expiryYear = Integer.parseInt(expiryDateString.substring(0, 2));

                    Card card = new Card.Builder()
                            .setNumber(pan)
                            .setExpiryDate(expiryMonth, expiryYear)
                            .build();

                    notifyCardRead(card);

                    return true;
                } catch (NumberFormatException e) {
                    // Ignore.
                }
            }
        }

        return false;
    }

    @Nullable
    private Response transceive(@NonNull IsoDep isoDep, @NonNull Command command) throws IOException {
        byte[] commandBytes = command.getBytes();
        byte[] responseBytes = isoDep.transceive(commandBytes);
        return Response.parseResponse(responseBytes);
    }

    private void readCard(@NonNull IsoDep isoDep) throws IOException {
        TagLengthValue fciTemplate = selectPseDirectory(isoDep);
        TagLengthValue applicationDataFile = null;

        if (fciTemplate != null) {
            notifyCardDiscovered();

            applicationDataFile = fciTemplate.find(Tags.APPLICATION_DATA_FILE);

            if (applicationDataFile == null) {
                TagLengthValue sfi = fciTemplate.find(Tags.SFI);
                TagLengthValue pseRecord = getPseRecord(isoDep, sfi);

                if (pseRecord != null) {
                    applicationDataFile = pseRecord.find(Tags.APPLICATION_DATA_FILE);
                }
            }
        }

        if (applicationDataFile != null) {
            readFromApplicationDataFile(isoDep, applicationDataFile);
        } else {
            notifyError(Error.CARD_UNSUPPORTED);
        }
    }

    private void readFromApplicationDataFile(@NonNull IsoDep isoDep, @Nullable TagLengthValue applicationDataFile) throws IOException {
        TagLengthValue applicationFciTemplate = selectApplication(isoDep, applicationDataFile);

        byte[] trackTwoEquivalentDataValue = null;
        byte[] applicationFileLocatorValues = null;

        if (applicationFciTemplate != null) {
            TagLengthValue tagLengthValue = sendPdolData(isoDep, applicationFciTemplate);

            if (tagLengthValue != null) {
                TagLengthValue dataObject = tagLengthValue.find(Tags.DATA_OBJECT_FORMAT_1);

                if (dataObject != null) {
                    TagLengthValue trackTwoEquivalentData = tagLengthValue.find(Tags.TRACK_TWO_EQUIVALENT_DATA);
                    trackTwoEquivalentDataValue = trackTwoEquivalentData != null ? trackTwoEquivalentData.getValue() : null;

                    if (trackTwoEquivalentDataValue == null) {
                        TagLengthValue applicationFileLocator = tagLengthValue.find(Tags.APPLICATION_FILE_LOCATOR);

                        if (applicationFileLocator != null) {
                            applicationFileLocatorValues = applicationFileLocator.getValue();
                        }
                    }
                } else {
                    byte[] value = tagLengthValue.getValue();
                    applicationFileLocatorValues = Arrays.copyOfRange(value, 2, value.length);
                }
            }
        }

        if ((trackTwoEquivalentDataValue == null || !checkNotifyListenerRead(trackTwoEquivalentDataValue))
                && (applicationFileLocatorValues == null || !readFromApplicationFileLocatorValue(isoDep, applicationFileLocatorValues))) {
            notifyError(Error.CARD_UNSUPPORTED);
        }
    }

    private boolean readFromApplicationFileLocatorValue(@NonNull IsoDep isoDep, @NonNull byte[] applicationFileLocatorValues) throws IOException {
        List<TagLengthValue> recordInformations = new ArrayList<>();
        List<ApplicationFileLocator> afls = Parser.parseList(applicationFileLocatorValues, ApplicationFileLocator.class);

        for (ApplicationFileLocator afl : afls) {
            recordInformations.addAll(getRecordInformation(isoDep, afl));
        }

        return checkNotifyListenerRead(recordInformations);
    }

    @Nullable
    private TagLengthValue selectPseDirectory(@NonNull IsoDep isoDep) throws IOException {
        TagLengthValue fciTemplate = selectPseDirectory(isoDep, PPSE);

        if (fciTemplate == null) {
            fciTemplate = selectPseDirectory(isoDep, PSE);
        }

        if (fciTemplate == null) {
            for (CardScheme cardScheme : CardScheme.values()) {
                for (byte[] applicationIdentifier : cardScheme.getApplicationIdentifiers()) {
                    fciTemplate = selectPseDirectory(isoDep, applicationIdentifier);

                    if (fciTemplate != null
                            && (fciTemplate.find(Tags.APPLICATION_DATA_FILE) != null || fciTemplate.find(Tags.SFI) != null)) {
                        break;
                    }
                }
            }
        }

        if (fciTemplate == null) {
            fciTemplate = selectPseDirectory(isoDep, new byte[0]);

            if (fciTemplate != null) {
                TagLengthValue dfName = fciTemplate.find(Tags.DF_NAME);
                byte[] dfNameValue = dfName != null ? dfName.getValue() : null;

                if (dfNameValue != null) {
                    fciTemplate = selectPseDirectory(isoDep, dfNameValue);
                }
            }
        }

        return fciTemplate;
    }

    @Nullable
    private TagLengthValue selectPseDirectory(@NonNull IsoDep isoDep, @NonNull byte[] pse) throws IOException {
        Command command = Command.select(pse);
        Response response = transceive(isoDep, command);
        return TagLengthValue.parseTagLengthValue(response != null ? response.getValue() : null);
    }

    @Nullable
    private TagLengthValue getPseRecord(@NonNull IsoDep isoDep, @Nullable TagLengthValue sfi) throws IOException {
        byte[] sfiBytes = sfi != null ? sfi.getValue() : null;

        Command command;
        Response response;

        if (sfiBytes != null && sfiBytes.length == 1) {
            byte sfiValue = sfiBytes[0];
            byte p2 = (byte) ((sfiValue << 3) | 4);

            command = Command.read((byte) 0x01, p2);
            response = transceive(isoDep, command);
        } else {
            command = Command.read((byte) 0x01, (byte) 0x00);
            response = transceive(isoDep, command);
        }

        if (response != null) {
            switch (response.getSw1()) {
                case Response.SW1_INVALID_LENGTH:
                case Response.SW1_WRONG_LENGTH:
                    // TODO: 30/08/2017 Handle, SW2 is correct length.
                    break;
                case Response.SW1_SUCCESS:
                    // noinspection ConstantConditions, SW1 indicates success.
                    return TagLengthValue.parseTagLengthValue(response.getValue()).find(Tags.PSE_RECORD);
                default:
                    // Fall through.
            }
        }

        return null;
    }

    @Nullable
    private TagLengthValue selectApplication(@NonNull IsoDep isoDep, @Nullable TagLengthValue applicationDataFile) throws IOException {
        TagLengthValue applicationId = applicationDataFile != null ? applicationDataFile.find(Tags.APPLICATION_ID) : null;
        byte[] applicationIdValue = applicationId != null ? applicationId.getValue() : null;

        if (applicationIdValue != null) {
            Command command = Command.select(applicationIdValue);
            Response response = transceive(isoDep, command);
            TagLengthValue fciTemplate = TagLengthValue.parseTagLengthValue(response != null ? response.getValue() : null);

            if (fciTemplate != null) {
                return fciTemplate.find(Tags.FCI_TEMPLATE);
            }
        }

        return null;
    }

    @Nullable
    private TagLengthValue sendPdolData(@NonNull IsoDep isoDep, @Nullable TagLengthValue applicationFciTemplate) throws IOException {
        byte[] data = PdolDataGenerator.create(applicationFciTemplate).getBytes();
        Command command = Command.getProcessingOptions(data);
        Response response = transceive(isoDep, command);
        return TagLengthValue.parseTagLengthValue(response != null ? response.getValue() : null);
    }

    @NonNull
    private List<TagLengthValue> getRecordInformation(@NonNull IsoDep isoDep, @NonNull ApplicationFileLocator applicationFileLocator)
            throws IOException {
        List<TagLengthValue> result = new ArrayList<>();

        for (byte p1 = applicationFileLocator.getFirstRecordIndex(); p1 <= applicationFileLocator.getLastRecordIndex(); p1++) {
            byte p2 = (byte) ((applicationFileLocator.getSfi() << 3) | 4);
            Command command = Command.read(p1, p2, new byte[0], new byte[0]);
            Response response = transceive(isoDep, command);

            if (response != null) {
                if (response.getSw1() == Response.SW1_INVALID_LENGTH || response.getSw1() == Response.SW1_WRONG_LENGTH) {
                    response = transceive(isoDep, Command.read(p1, p2, new byte[0], response.getSw2()));
                }
            }

            if (response != null) {
                TagLengthValue recordInformation = TagLengthValue.parseTagLengthValue(response.getValue());

                if (recordInformation != null) {
                    result.add(recordInformation);
                }
            }
        }

        return result;
    }

    /**
     * Listener interface for a {@link NfcCardReader}.
     */
    public interface Listener {
        /**
         * Called when an NFC chip was discovered.
         *
         * @param supported Flag indicating whether this chip uses a supported technology.
         */
        void onChipDiscovered(boolean supported);

        /**
         * Called when a card was discovered.
         */
        void onCardDiscovered();

        /**
         * Called when a {@link Card} was read.
         *
         * @param card The result {@link Card}.
         */
        void onCardRead(@NonNull Card card);

        /**
         * Called when an error was encountered.
         *
         * @param error The {@link Error}.
         */
        void onError(@NonNull Error error);
    }

    /**
     * Errors for {@link Listener#onError(Error)}.
     */
    public enum Error {
        /**
         * The card is not supported by the {@link NfcCardReader}.
         */
        CARD_UNSUPPORTED,
        /**
         * The connection to the card was lost.
         */
        CONNECTION_LOST,
        /**
         * NFC is unavailable on the device.
         */
        UNAVAILABLE
    }

    private final class NfcReaderCallback implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag) {
            try {
                IsoDep isoDep = IsoDep.get(tag);

                if (isoDep != null) {
                    notifyChipDiscovered(true);
                    isoDep.setTimeout(5000);
                    isoDep.connect();
                    readCard(isoDep);
                    isoDep.close();
                } else {
                    notifyChipDiscovered(false);
                }
            } catch (IOException e) {
                notifyError(Error.CONNECTION_LOST);
            }
        }
    }
}
