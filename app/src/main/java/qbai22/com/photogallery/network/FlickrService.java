package qbai22.com.photogallery.network;

import android.support.annotation.Nullable;

import qbai22.com.photogallery.model.Flickr;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface FlickrService {

    @GET("?method=flickr.photos.getRecent")
    Call<Flickr> getPageFlickr(@Query("page") Integer page);

    @GET("?method=flickr.photos.search")
    Call<Flickr> searchFlickr(@Query("page") Integer page, @Query("text") @Nullable String text);

}
