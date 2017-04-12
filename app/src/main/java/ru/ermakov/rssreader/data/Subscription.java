package ru.ermakov.rssreader.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Подписка.
 */
public class Subscription implements Parcelable {
    private int mId;
    private String mName;
    private String mUrl;

    public Subscription(int id, String name, String url) {
        this(name, url);
        this.mId = id;
    }

    public Subscription(String name, String url) {
        this.mId = -1;
        this.mName = name;
        this.mUrl = url;
    }

    protected Subscription(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mUrl = in.readString();
    }

    public static final Creator<Subscription> CREATOR = new Creator<Subscription>() {
        @Override
        public Subscription createFromParcel(Parcel in) {
            return new Subscription(in);
        }

        @Override
        public Subscription[] newArray(int size) {
            return new Subscription[size];
        }
    };

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mUrl);
    }
}
