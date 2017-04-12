package ru.ermakov.rssreader.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import ru.ermakov.rssreader.R;
import ru.ermakov.rssreader.data.Subscription;

/**
 * Диалоговое окно для добавления новой RSS-подписки.
 */
public class AddSubscriptionDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = SubscriptionsFragment.class.getSimpleName();

    public static final String EXTRA_SUBSCRIPTION = "EXTRA_SUBSCRIPTION";

    EditText mNameEditText;
    EditText mUrlEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_subscription_dialog, container, false);

        mNameEditText = (EditText) view.findViewById(R.id.et_subscription_name);
        mUrlEditText = (EditText) view.findViewById(R.id.et_subscription_url);
        Button addButton = (Button) view.findViewById(R.id.btn_add);
        Button cancelButton = (Button) view.findViewById(R.id.btn_cancel);

        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                Subscription subscription = createSubscription();
                if (subscription != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_SUBSCRIPTION, subscription);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    dismiss();
                }
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    /**
     * Собрать объект Subscription.
     */
    private Subscription createSubscription() {
        String subscriptionName = mNameEditText.getText().toString();
        String subscriptionUrl = mUrlEditText.getText().toString();

        if (TextUtils.isEmpty(subscriptionName)) {
            mNameEditText.setError(getResources().getString(R.string.err_subscription_is_empty));
            return null;
        }

        if (TextUtils.isEmpty(subscriptionUrl)) {
            mUrlEditText.setError(getResources().getString(R.string.err_subscription_url_is_empty));
            return null;
        }

        return new Subscription(subscriptionName, subscriptionUrl);
    }
}
