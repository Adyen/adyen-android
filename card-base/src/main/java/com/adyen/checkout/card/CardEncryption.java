/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/5/2019.
 */

package com.adyen.checkout.card;

import android.support.annotation.NonNull;

import com.adyen.checkout.card.data.output.CardOutputData;
import com.adyen.checkout.card.model.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;

import java.util.Date;

public interface CardEncryption {
    @NonNull
    EncryptedCard encryptCardOutput(@NonNull CardOutputData cardOutputData, @NonNull String publicKey, @NonNull Date generationTime)
            throws EncryptionException;
}
