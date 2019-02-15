/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ft.shippo.adapters;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;

/**
 * InfiniteScrollListener, which can be added to RecyclerView with addOnScrollListener
 * to detect moment when RecyclerView was scrolled to the end.
 */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private static final String TAG = "InfiniteScrollListener";
    private final int maxItemsPerRequest;
    private final LayoutManager layoutManager;
    private boolean shouldLoadMore = true;

    /**
     * Initializes InfiniteScrollListener, which can be added
     * to RecyclerView with addOnScrollListener method
     *
     * @param maxItemsPerRequest Max items to be loaded in a single request.
     * @param layoutManager      LinearLayoutManager created in the Activity.
     */
    public InfiniteScrollListener(int maxItemsPerRequest, LayoutManager layoutManager) {
        checkIfPositive(maxItemsPerRequest, "maxItemsPerRequest <= 0");
        checkNotNull(layoutManager, "layoutManager == null");
        this.maxItemsPerRequest = maxItemsPerRequest;
        this.layoutManager = layoutManager;
    }

    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkIfPositive(int number, String message) {
        if (number <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Callback method to be invoked when the RecyclerView has been scrolled
     *
     * @param recyclerView The RecyclerView which scrolled.
     * @param dx           The amount of horizontal scroll.
     * @param dy           The amount of vertical scroll.
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (canLoadMoreItems()) {
            if (layoutManager instanceof GridLayoutManager)
                onScrolledToEnd(((GridLayoutManager) layoutManager).findFirstVisibleItemPosition());
            else
                onScrolledToEnd(((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition());
        }
    }

    /**
     * Refreshes RecyclerView by setting new adapter,
     * calling invalidate method and scrolling to given position
     *
     * @param view     RecyclerView to be refreshed
     * @param adapter  adapter with new list of items to be loaded
     * @param position position to which RecyclerView will be scrolled
     */
    protected void refreshView(RecyclerView view, RecyclerView.Adapter adapter, int position) {
        view.setAdapter(adapter);
        view.invalidate();
        view.scrollToPosition(position);
    }

    /**
     * Checks if more items can be loaded to the RecyclerView
     *
     * @return boolean Returns true if can load more items or false if not.
     */
    protected boolean canLoadMoreItems() {
        if (!shouldLoadMore) return false;

        if (layoutManager instanceof GridLayoutManager) {
            final int visibleItemsCount = ((GridLayoutManager) layoutManager).getChildCount();
            final int totalItemsCount = ((GridLayoutManager) layoutManager).getItemCount();
            final int pastVisibleItemsCount = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
            final boolean lastItemShown = visibleItemsCount + pastVisibleItemsCount >= totalItemsCount;
            return lastItemShown && totalItemsCount >= maxItemsPerRequest;
        } else {
            final int visibleItemsCount = ((LinearLayoutManager) layoutManager).getChildCount();
            final int totalItemsCount = ((LinearLayoutManager) layoutManager).getItemCount();
            final int pastVisibleItemsCount = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            final boolean lastItemShown = visibleItemsCount + pastVisibleItemsCount >= totalItemsCount;
            return lastItemShown && totalItemsCount >= maxItemsPerRequest;

        }
    }

    /**
     * Callback method to be invoked when the RecyclerView has been scrolled to the end
     *
     * @param firstVisibleItemPosition Id of the first visible item on the list.
     */
    public abstract void onScrolledToEnd(final int firstVisibleItemPosition);

    public void shouldLoadMore(boolean should) {
    }

    public void showLoadMore(boolean b) {

    }
}