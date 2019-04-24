package com.app.legend.dms.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

/**
 * model
 */
@Data
public class HideComic implements Parcelable {

    private String id;
    private String title;
    private String author;
    private String bookLink;
    private String status;
    private String description;

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
