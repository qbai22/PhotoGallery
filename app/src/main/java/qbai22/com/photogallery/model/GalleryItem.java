package qbai22.com.photogallery.model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by qbai on 17.10.2016.
 */

public class GalleryItem {
    public String id;
    public String title;
    public String owner;
    @SerializedName("url_s")
    public String mUrl;

    public String getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return mUrl;
    }

    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build();
    }
}
