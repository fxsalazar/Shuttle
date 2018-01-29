package com.simplecity.amp_library.ui.fragments;

import android.support.annotation.Nullable;

public interface Controller<T> {

    /**
     * @return the parent {@link NavigationController}, or null if none exists.
     */
    @Nullable
    NavigationController<T> getNavigationController();

}