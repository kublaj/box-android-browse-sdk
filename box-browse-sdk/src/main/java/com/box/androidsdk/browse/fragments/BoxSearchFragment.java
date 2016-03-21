package com.box.androidsdk.browse.fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.box.androidsdk.browse.R;
import com.box.androidsdk.browse.adapters.BoxSearchListAdapter;
import com.box.androidsdk.browse.service.BoxResponseIntent;
import com.box.androidsdk.browse.uidata.BoxListItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsSearch;

import java.io.File;

/**
 * Use the {@link com.box.androidsdk.browse.fragments.BoxSearchFragment.Builder} factory method to
 * create an instance of this fragment.
 */
public class BoxSearchFragment extends BoxBrowseFragment {
    private static final String OUT_ITEM = "outItem";

    private static BoxRequestsSearch.Search mRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getSerializable(OUT_ITEM) instanceof BoxRequestsSearch.Search) {
            mRequest = (BoxRequestsSearch.Search) getArguments().getSerializable(OUT_ITEM);
            setToolbar(mRequest.getQuery());
        }
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter filter = super.getIntentFilter();
        filter.addAction(BoxRequestsSearch.Search.class.getName());
        return filter;
    }

    public void search(BoxRequestsSearch.Search request) {
        setToolbar(request.getQuery());
        setListItems(new BoxIteratorItems());
        mAdapter.removeAll();
        loadItems();
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void loadItems() {
        mRequest.setLimit(mLimit)
                .setOffset(0);
        mController.execute(mRequest);
    }

    @Override
    protected void updateItems(BoxIteratorItems items) {
        super.updateItems(items);

        // If not all entries were fetched add a task to fetch more items if user scrolls to last entry.
        if (items.fullSize() != null && mBoxIteratorItems.size() < items.fullSize()) {
            // The search endpoint returns a 400 bad request if the offset is not in multiples of the limit
            int multiplier = mBoxIteratorItems.size() / mLimit;
            BoxRequestsSearch.Search incrementalSearchTask = mRequest
                    .setOffset(multiplier * mLimit)
                    .setLimit(mLimit);
            mAdapter.add(new BoxListItem(incrementalSearchTask, ACTION_FUTURE_TASK));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(OUT_ITEM, mRequest);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void handleResponse(BoxResponseIntent intent) {
        super.handleResponse(intent);
        if (intent.getAction().equals(BoxRequestsSearch.Search.class.getName())) {
            onItemsFetched(intent);
        }
    }

    private void onItemsFetched(BoxResponseIntent intent) {
        checkConnectivity();

        if (mAdapter == null) {
            return;
        }

        // On failure updates the existing loading item with an error item
        if (!intent.isSuccess()) {
            BoxListItem item = mAdapter.get(intent.getAction());
            if (item != null) {
                item.setResponse(intent);
                item.setState(BoxListItem.State.ERROR);
                mAdapter.update(intent.getAction());
            }
            Toast.makeText(getActivity(), getResources().getString(R.string.box_browsesdk_problem_performing_search), Toast.LENGTH_LONG).show();
            return;
        }

        // On success should remove the existing loading item
        mAdapter.remove(intent.getAction());
        if (intent.getResult() instanceof BoxIteratorItems) {
            BoxIteratorItems collection = (BoxIteratorItems) intent.getResult();
            updateItems(collection);
        }
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onBindBoxItemViewHolder(BoxItemViewHolder holder) {
        if (holder.getItem() == null || holder.getItem().getBoxItem() == null) {
            return;
        }
        final BoxItem item = holder.getItem().getBoxItem();
        holder.getNameView().setText(item.getName());
        holder.getMetaDescription().setText(BoxSearchListAdapter.createPath(item, File.separator));
        mThumbnailManager.setThumbnailIntoView(holder.getThumbView(), item);
        holder.getProgressBar().setVisibility(View.GONE);
        holder.getMetaDescription().setVisibility(View.VISIBLE);
        holder.getThumbView().setVisibility(View.VISIBLE);
    }

    /**
     * Builder for constructing an instance of BoxBrowseFolderFragment
     */
    public static class Builder extends BoxBrowseFragment.Builder<BoxSearchFragment> {

        /**
         * @param session
         * @param searchRequest
         */
        public Builder(BoxSession session, BoxRequestsSearch.Search searchRequest) {
            mRequest = searchRequest;
            mArgs.putString(ARG_USER_ID, session.getUserId());
            mArgs.putSerializable(OUT_ITEM, mRequest);
            mArgs.putInt(ARG_LIMIT, DEFAULT_LIMIT);

        }

        /**
         * @param session
         * @param query
         */
        public Builder(BoxSession session, String query) {
            mArgs.putString(ARG_USER_ID, session.getUserId());
            mArgs.putSerializable(OUT_ITEM, mRequest);
            mArgs.putInt(ARG_LIMIT, DEFAULT_LIMIT);
        }

        @Override
        protected BoxSearchFragment getInstance() {
            return new BoxSearchFragment();
        }
    }
}
