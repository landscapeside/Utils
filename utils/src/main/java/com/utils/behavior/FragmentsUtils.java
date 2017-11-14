/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.utils.behavior;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.text.TextUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This provides methods to help Activities load their UI.
 */
public class FragmentsUtils {

    static List<WeakReference<Fragment>> loadedFragments = new ArrayList<>();

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     *
     */
    public static void addFragmentToActivity (FragmentManager fragmentManager,
                                              Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
        if (!containsFragment(fragment)) {
            loadedFragments.add(new WeakReference<>(fragment));
        }
    }

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     *
     */
    public static void addFragmentToActivityStateLoss (FragmentManager fragmentManager,
                                              Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commitAllowingStateLoss();
        if (!containsFragment(fragment)) {
            loadedFragments.add(new WeakReference<>(fragment));
        }
    }

    public static void replaceFragmentToActivity(FragmentManager fragmentManager,
                                                 Fragment fragment, int frameId){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commit();
        if (!containsFragment(fragment)) {
            loadedFragments.add(new WeakReference<>(fragment));
        }
    }

    public static void showFragmentToActivity(FragmentManager fragmentManager,
                                              Fragment from,Fragment to, int frameId){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(from);
        if (!containsFragment(to)) {
            transaction.add(frameId, to);
            loadedFragments.add(new WeakReference<>(to));
        } else {
            transaction.show(to);
        }
        transaction.commit();
    }

    public static void unInstall() {
        loadedFragments.clear();
    }

    public static boolean containsFragment(Fragment fragment){
        boolean result = false;
        for (WeakReference<Fragment> ref:loadedFragments) {
            if (ref.get() == fragment){
                return true;
            }
        }
        return result;
    }

    public static <T extends Fragment> T turnToFragment(FragmentManager fragmentManager,
        int containerId, Class<T> fragmentClass, Bundle args) {
        return turnToFragment(fragmentManager, containerId, fragmentClass, args, false, null);
    }

    public static <T extends Fragment> T turnToFragment(FragmentManager fragmentManager,
        int containerId, Class<T> fragmentClass, Bundle args, boolean backAble, String customTag) {
        String tag = fragmentClass.getName();
        if (!TextUtils.isEmpty(customTag)) {
            tag = customTag;
        }
        T fragment = (T) fragmentManager.findFragmentByTag(tag);
        boolean isFragmentExist = true;
        if (fragment == null) {
            try {
                isFragmentExist = false;
                fragment = fragmentClass.newInstance();
                fragment.setArguments(new Bundle());
            } catch (Exception e) {
                throw new Fragment.InstantiationException("can't instance : " + fragmentClass.getName(), e);
            }
        }
        if (fragment.isAdded()) {
            return fragment;
        }
        if (args != null && !args.isEmpty()) {
            fragment.getArguments().putAll(args);
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in,
            android.R.anim.fade_out);
        if (isFragmentExist) {
            ft.replace(containerId, fragment);
        } else {
            ft.replace(containerId, fragment, tag);
        }
        if (backAble) {
            ft.addToBackStack(tag);
        }
        ft.commitAllowingStateLoss();
        return fragment;
    }

}
