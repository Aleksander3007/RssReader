package ru.ermakov.rssreader.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ermakov.rssreader.R;
import ru.ermakov.rssreader.net.RssResponse;

/**
 * Фрагмент для работы с деталями поста.
 */
public class PostDetailFragment extends Fragment {

    public static final String ARG_POST = "ARG_POST";

    public static PostDetailFragment newInstance(RssResponse.Post post) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_POST, post);

        PostDetailFragment fragment = new PostDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        RssResponse.Post post = getArguments().getParcelable(ARG_POST);

        TextView postTitleTextView = (TextView) view.findViewById(R.id.tv_post_title);
        TextView postDescriptionTextView = (TextView) view.findViewById(R.id.tv_post_description);

        postTitleTextView.setText(post.getTitle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            postDescriptionTextView.setText(Html.fromHtml(post.getDescription(), Html.FROM_HTML_MODE_LEGACY));
        }
        else {
            postDescriptionTextView.setText(Html.fromHtml(post.getDescription()));
        }

        return view;
    }
}
