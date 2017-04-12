package ru.ermakov.rssreader.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ru.ermakov.rssreader.R;
import ru.ermakov.rssreader.RssContentProvider;
import ru.ermakov.rssreader.data.Subscription;
import ru.ermakov.rssreader.adapters.SubscriptionsAdapter;
import ru.ermakov.rssreader.db.PostEntry;
import ru.ermakov.rssreader.db.SubscriptionEntry;

/**
 * Фрагмент для работы с RSS-подписками.
 */
public class SubscriptionsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener,
        SubscriptionsAdapter.OnSubscriptionClickListener {

    public static final String TAG = SubscriptionsFragment.class.getSimpleName();

    private RecyclerView mSubscriptionsRecyclerView;

    private static final int ID_SUBSCRIPTIONS_LOADER = 1;
    private static final int ID_INSERT_SUBSCRIPTION_LOADER = 2;
    private static final int ID_DELETE_SUBSCRIPTION_LOADER = 3;

    private static final int DIALOG_FRAGMENT = 1;

    private SubscriptionsAdapter mSubscriptionsAdapter = new SubscriptionsAdapter(this);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        mSubscriptionsRecyclerView = (RecyclerView) view.findViewById(R.id.rv_subscriptions);
        initSubscriptionsRecyclerView();

        FloatingActionButton addNewSubscriptionBtn = (FloatingActionButton)
                view.findViewById(R.id.fab_add_new_subscription);
        addNewSubscriptionBtn.setOnClickListener(this);

        getLoaderManager().restartLoader(ID_SUBSCRIPTIONS_LOADER, null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), RssContentProvider.SUBSCRIPTIONS_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null)
            mSubscriptionsAdapter.setSubscriptionsCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_add_new_subscription) {
            showAddSubscriptionsDialog();
        }
    }

    /**
     * Нажатие в RecyclerView на элемент, содержащий определенную подписку.
     * @param subscription подписка, которая была нажата.
     */
    @Override
    public void onSubscriptionClick(Subscription subscription) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.l_fragment_container, PostsFragment.newInstance(subscription))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    Subscription subscription =
                            data.getParcelableExtra(AddSubscriptionDialog.EXTRA_SUBSCRIPTION);
                    addSubscription(subscription);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initSubscriptionsRecyclerView() {
        mSubscriptionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSubscriptionsRecyclerView.setAdapter(mSubscriptionsAdapter);
        addItemTouchHelperToRecyclerView();
    }

    /**
     * Добавление подписки в БД.
     * @param subscription подписка, которую необходимо добавить.
     */
    public void addSubscription(final Subscription subscription) {
        getLoaderManager().restartLoader(ID_INSERT_SUBSCRIPTION_LOADER, null,
                new LoaderManager.LoaderCallbacks<Subscription>() {
                    @Override
                    public Loader<Subscription> onCreateLoader(int id, Bundle args) {
                        return new AsyncTaskLoader<Subscription>(getContext()) {
                            @Override
                            public Subscription loadInBackground() {

                                ContentValues contentValues = new ContentValues();
                                contentValues.put(SubscriptionEntry.COLUMN_NAME, subscription.getName());
                                contentValues.put(SubscriptionEntry.COLUMN_URL, subscription.getUrl());

                                getContext().getContentResolver().insert(
                                        RssContentProvider.SUBSCRIPTIONS_URI,
                                        contentValues
                                );

                                return subscription;
                            }
                        };
                    }

                    @Override
                    public void onLoadFinished(Loader<Subscription> loader, Subscription data) {

                    }

                    @Override
                    public void onLoaderReset(Loader<Subscription> loader) {

                    }
                }).forceLoad();
    }

    private void showAddSubscriptionsDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        AddSubscriptionDialog addSubscriptionDialog = new AddSubscriptionDialog();
        addSubscriptionDialog.setTargetFragment(this, DIALOG_FRAGMENT);
        addSubscriptionDialog.show(fragmentManager.beginTransaction(), "addSubscriptionDialog");
    }

    /**
     * Добавить ItemTouchHelper к RecyclerView.
     */
    private void addItemTouchHelperToRecyclerView() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            // Удаляем подписку по swipe.
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int subscriptionId = (int) viewHolder.itemView.getTag();
                getLoaderManager().restartLoader(ID_DELETE_SUBSCRIPTION_LOADER, null,
                        new LoaderManager.LoaderCallbacks<Boolean>() {
                            @Override
                            public Loader<Boolean> onCreateLoader(int id, Bundle args)
                            {
                                return new AsyncTaskLoader<Boolean>(getContext()) {
                                    @Override
                                    public Boolean loadInBackground()
                                    {
                                        int nDeleted = getContext().getContentResolver().delete(
                                                RssContentProvider.SUBSCRIPTIONS_URI,
                                                SubscriptionEntry._ID + " = ?",
                                                new String[]{String.valueOf(subscriptionId)}
                                        );

                                        return (nDeleted > 0);
                                    }
                                };
                            }

                            @Override
                            public void onLoadFinished(Loader<Boolean> loader, Boolean data)
                            {
                                if (data == false) {
                                    Toast.makeText(getContext(),
                                            getResources().getString(R.string.err_delete_subscription),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onLoaderReset(Loader<Boolean> loader)
                            {

                            }
                        }).forceLoad();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mSubscriptionsRecyclerView);
    }
}
