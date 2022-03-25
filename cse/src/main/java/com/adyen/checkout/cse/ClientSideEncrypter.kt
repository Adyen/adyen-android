/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */

package com.adyen.checkout.cse;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.adyen.checkout.cse.exception.EncryptionException;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import kotlin.text.Charsets;


/**
 * Created by andrei on 8/8/16.
 */
public class ClientSideEncrypter {

    private static final String PREFIX = "adyenan";
    private static final String VERSION = "0_1_1";
    private static final String SEPARATOR = "$";

    private final Cipher mAesCipher;
    private final Cipher mRsaCipher;
    private final SecureRandom mSecureRandom;

    public ClientSideEncrypter(@NonNull String publicKeyString) throws EncryptionException {
        if (!ValidationUtils.isPublicKeyValid(publicKeyString)) {
            throw new EncryptionException("Invalid public key: " + publicKeyString, null);
        }

        mSecureRandom = new SecureRandom();
        final String[] keyComponents = publicKeyString.split("\\|");

        // The bytes can be converted back to a public key object
        final KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("RSA KeyFactory not found.", e);
        }

        final int radix = 16;
        final RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(
                new BigInteger(keyComponents[1].toLowerCase(Locale.getDefault()), radix),
                new BigInteger(keyComponents[0].toLowerCase(Locale.getDefault()), radix));

        final PublicKey pubKey;
        try {
            pubKey = keyFactory.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new EncryptionException("Problem reading public key: " + publicKeyString, e);
        }

        try {
            mAesCipher = Cipher.getInstance("AES/CCM/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Problem instantiation AES Cipher Algorithm", e);
        } catch (NoSuchPaddingException e) {
            throw new EncryptionException("Problem instantiation AES Cipher Padding", e);
        }

        try {
            mRsaCipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            mRsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);

        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Problem instantiation RSA Cipher Algorithm", e);
        } catch (NoSuchPaddingException e) {
            throw new EncryptionException("Problem instantiation RSA Cipher Padding", e);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("Invalid public key: " + publicKeyString, e);
        }
    }

    @NonNull
    public String encrypt(@NonNull String plainText) throws EncryptionException {
        final SecretKey aesKey = generateAesKey();

        final byte[] iv = generateIV();

        final byte[] encrypted;
        try {
            mAesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
            encrypted = mAesCipher.doFinal(plainText.getBytes(Charsets.UTF_8));
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Incorrect AES Block Size", e);
        } catch (BadPaddingException e) {
            throw new EncryptionException("Incorrect AES Padding", e);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("Invalid AES Key", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Invalid AES Parameters", e);
        }

        final byte[] result = new byte[iv.length + encrypted.length];
        // copy IV to result
        System.arraycopy(iv, 0, result, 0, iv.length);
        // copy encrypted to result
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        final byte[] encryptedAesKey;
        try {
            encryptedAesKey = mRsaCipher.doFinal(aesKey.getEncoded());
            return String.format(
                    "%s%s%s%s%s%s",
                    PREFIX,
                    VERSION,
                    SEPARATOR,
                    Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP), SEPARATOR,
                    Base64.encodeToString(result, Base64.NO_WRAP)
            );
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Incorrect RSA Block Size", e);
        } catch (BadPaddingException e) {
            throw new EncryptionException("Incorrect RSA Padding", e);
        }
    }

    private SecretKey generateAesKey() throws EncryptionException {
        final int keySize = 256;
        final KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Unable to get AES algorithm", e);
        }
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    /**
     * Generate a random Initialization Vector (IV).
     *
     * @return the IV bytes
     */
    private byte[] generateIV() {
        final int ivSize = 12;
        //generate random IV AES is always 16bytes, but in CCM mode this represents the NONCE
        final byte[] iv = new byte[ivSize];
        mSecureRandom.nextBytes(iv);
        return iv;
    }

}
