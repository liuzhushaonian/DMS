package com.app.legend.dms.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * model
 */
public class HideComic implements Parcelable {

    private String id;
    private String title;
    private String author;
    private String bookLink;

    public HideComic() {
    }


    protected HideComic(Parcel in) {
        id = in.readString();
        title = in.readString();
        author = in.readString();
        bookLink = in.readString();
    }

    public static final Creator<HideComic> CREATOR = new Creator<HideComic>() {
        @Override
        public HideComic createFromParcel(Parcel in) {
            return new HideComic(in);
        }

        @Override
        public HideComic[] newArray(int size) {
            return new HideComic[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBookLink() {
        return bookLink;
    }

    public void setBookLink(String bookLink) {
        this.bookLink = bookLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(bookLink);
    }
}
