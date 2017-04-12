package ru.ermakov.rssreader.net;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Ответ от сервера, предоставляющего RSS-каналю
 */
@Root(name = "rss")
public class RssResponse {

    @Element(name="channel")
    private Channel mChannel;

    public List<Post> getPosts() {
        return mChannel.getPosts();
    }

    public static class Channel {
        @ElementList(inline = true)
        private List<Post> mPosts;

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("Posts: [");
            for (Post post : mPosts) {
                stringBuilder.append(post.toString());
                stringBuilder.append("\n");
            }
            stringBuilder.append("]");

            return stringBuilder.toString();
        }

        public List<Post> getPosts() {
            return mPosts;
        }
    }

    @Root(name="item")
    public static class Post implements Parcelable {
        @Element(name="title")
        private String mTitle;

        @Element(name="description", required=false)
        private String mDescription;

        /** Конструктор необходим для SimpleXmlFramework. */
        public Post() {}

        public Post(String title, String description) {
            this.mTitle = title;
            this.mDescription = description;
        }

        protected Post(Parcel in) {
            mTitle = in.readString();
            mDescription = in.readString();
        }

        public static final Creator<Post> CREATOR = new Creator<Post>() {
            @Override
            public Post createFromParcel(Parcel in) {
                return new Post(in);
            }

            @Override
            public Post[] newArray(int size) {
                return new Post[size];
            }
        };

        @Override
        public String toString() {
            return String.format("{title: %s, description: %s}", getTitle(), getDescription());
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mTitle);
            dest.writeString(mDescription);
        }
    }

    @Override
    public String toString() {
        return mChannel.toString();
    }
}
