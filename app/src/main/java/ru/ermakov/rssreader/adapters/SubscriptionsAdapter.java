package ru.ermakov.rssreader.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ermakov.rssreader.R;
import ru.ermakov.rssreader.data.Subscription;
import ru.ermakov.rssreader.db.SubscriptionEntry;

/**
 * Адаптер для отображения RSS-подписок в RecyclerView.
 */
public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.ViewHolder> {

    Cursor mSubscriptionsCursor;

    /**
     * Интерфейс, который определяет методы для обработки нажатий на элементы.
     */
    public interface OnSubscriptionClickListener {
        /**
         * Обработка нажатия на элемент списка.
         * @param subscription подписка, на которую было выполнено нажатие.
         */
        void onSubscriptionClick(Subscription subscription);
    }
    private final OnSubscriptionClickListener mSubscriptionClickListener;

    public SubscriptionsAdapter(OnSubscriptionClickListener listener) {
        this.mSubscriptionClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Получаем индексы столбцов в курсоре для дальнейшего обращения по ним.
        int idIndex = mSubscriptionsCursor.getColumnIndex(SubscriptionEntry._ID);
        int nameIndex = mSubscriptionsCursor.getColumnIndex(SubscriptionEntry.COLUMN_NAME);

        mSubscriptionsCursor.moveToPosition(position);

        holder.itemView.setTag(mSubscriptionsCursor.getInt(idIndex));
        holder.setSubscriptionName(mSubscriptionsCursor.getString(nameIndex));
    }

    @Override
    public int getItemCount() {
        return (mSubscriptionsCursor != null) ? mSubscriptionsCursor.getCount() : 0;
    }

    public void setSubscriptionsCursor(Cursor newCursor) {
        // Если они одинаковые ничего менять не надо.
        if (mSubscriptionsCursor != newCursor) {
            mSubscriptionsCursor = newCursor;
            notifyDataSetChanged();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.tv_subscription_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mSubscriptionClickListener != null) {
                mSubscriptionClickListener.onSubscriptionClick(createSubscription());
            }
        }

        public void setSubscriptionName(CharSequence name) {
            mNameTextView.setText(name);
        }

        /**
         * Сформировать объект Subscription.
         * @return
         */
        private Subscription createSubscription() {

            int subscriptionId = (int)itemView.getTag();
            String subscriptionName = mNameTextView.getText().toString();

            mSubscriptionsCursor.moveToPosition(getAdapterPosition());
            Subscription subscription = new Subscription(subscriptionId, subscriptionName,
                    SubscriptionEntry.getUrl(mSubscriptionsCursor));

            return subscription;
        }
    }
}
