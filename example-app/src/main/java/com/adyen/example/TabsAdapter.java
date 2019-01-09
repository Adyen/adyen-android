/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 04/12/2018.
 */

package com.adyen.example;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import io.reactivex.annotations.Nullable;

public class TabsAdapter extends FragmentPagerAdapter {

    public static final int FRAGMENT_POSITION_SHOPPING_CART = 0;

    public static final int FRAGMENT_POSITION_CONFIGURATION = FRAGMENT_POSITION_SHOPPING_CART + 1;

    private static final int FRAGMENT_COUNT = FRAGMENT_POSITION_CONFIGURATION + 1;

    private ConfigurationFragment mConfigurationFragment;

    public TabsAdapter(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Nullable
    public ConfigurationFragment getConfigurationFragment() {
        return mConfigurationFragment;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case FRAGMENT_POSITION_SHOPPING_CART:
                return new ShoppingCartFragment();
            case FRAGMENT_POSITION_CONFIGURATION:
                return new ConfigurationFragment();
            default:
                throw new IllegalArgumentException("Invalid position.");
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);

        if (position == FRAGMENT_POSITION_CONFIGURATION) {
            mConfigurationFragment = (ConfigurationFragment) fragment;
        }

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        if (object == mConfigurationFragment) {
            mConfigurationFragment = null;
        }
    }
}
