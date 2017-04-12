package ru.ermakov.rssreader.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.ermakov.rssreader.R;
import ru.ermakov.rssreader.net.RssResponse;

/**
 * Адаптер для отображения постов RSS-подписки в RecyclerView.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<RssResponse.Post> mPosts;

    /**
     * Интерфейс, который определяет методы для обработки нажатий на элементы.
     */
    public interface OnPostClickListener {
        /**
         * Обработка нажатия на элемент списка.
         * @param post пост, на который было выполнено нажатие.
         */
        void onPostClick(RssResponse.Post post);
    }
    private final PostsAdapter.OnPostClickListener mOnPostClickListener;

    public PostsAdapter(PostsAdapter.OnPostClickListener listener) {
        this.mOnPostClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RssResponse.Post post = mPosts.get(position);
        holder.setPostTitle(post.getTitle());
    }

    @Override
    public int getItemCount() {
        return (mPosts != null) ? mPosts.size() : 0;
    }

    public void setPosts(List<RssResponse.Post> posts) {
        if (mPosts == null) {
            mPosts = posts;
        }
        else {
            mPosts.clear();
            mPosts.addAll(posts);
        }

        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTitleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.tv_post_title);
            itemView.setOnClickListener(this);
        }

        public void setPostTitle(CharSequence title) {
            mTitleTextView.setText(title);
        }

        @Override
        public void onClick(View v) {
            if (mOnPostClickListener != null)
                mOnPostClickListener.onPostClick(mPosts.get(getAdapterPosition()));
        }
    }
}
