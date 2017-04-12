package ru.ermakov.rssreader.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.ermakov.rssreader.R;
import ru.ermakov.rssreader.adapters.PostsAdapter;
import ru.ermakov.rssreader.data.Subscription;
import ru.ermakov.rssreader.loaders.PostsLoader;
import ru.ermakov.rssreader.net.RssResponse;

/**
 * Фрагмент для работы с постами подписки.
 */
public class PostsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<RssResponse.Post>>,
        PostsAdapter.OnPostClickListener {

    public static final String TAG = PostsFragment.class.getSimpleName();

    public static final String ARG_SUBSCRIPTION = "ARG_SUBSCRIPTION";

    private static final int ID_POSTS_LOADER = 1;

    private RecyclerView mPostsRecyclerView;

    private Subscription mSubscription;
    private PostsAdapter mPostsAdapter = new PostsAdapter(this);

    /**
     * Создание PostsFragment.
     * @param subscription подписка, для которой будут отображены посты в создаваемом фрагменте.
     * @return экземпляр PostsFragment.
     */
    public static PostsFragment newInstance(Subscription subscription) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_SUBSCRIPTION, subscription);

        PostsFragment fragment = new PostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        mPostsRecyclerView = (RecyclerView) view.findViewById(R.id.rv_posts);
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mPostsRecyclerView.setAdapter(mPostsAdapter);

        mSubscription = getArguments().getParcelable(ARG_SUBSCRIPTION);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().restartLoader(ID_POSTS_LOADER, null, this);
    }

    @Override
    public Loader<List<RssResponse.Post>> onCreateLoader(int id, Bundle args) {
        if (id == ID_POSTS_LOADER) {
            return new PostsLoader(getContext(), mSubscription);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<RssResponse.Post>> loader, List<RssResponse.Post> data) {
        if (data != null) {
            mPostsAdapter.setPosts(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<RssResponse.Post>> loader) {

    }

    /**
     * Нажатие в RecyclerView на элемент, содержащий определенный пост.
     * @param post пост, который был нажат.
     */
    @Override
    public void onPostClick(RssResponse.Post post) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.l_fragment_container, PostDetailFragment.newInstance(post))
                .addToBackStack(null)
                .commit();
    }
}
