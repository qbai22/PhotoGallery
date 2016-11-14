package qbai22.com.photogallery.network;

import android.support.annotation.Nullable;

import qbai22.com.photogallery.model.Flickr;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface FlickrService {

    final String API_KEY = "cc71ad3c1fb96e8f81ffc3a5d6d81ade";

    Retrofit rerofit = new Retrofit.Builder()
            .baseUrl("https://api.flickr.com/services/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @GET("?method=flickr.photos.getRecent" +
            "&api_key=" + API_KEY +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s")
    Call<Flickr> getPageFlickr(@Query("page") Integer page);

    @GET("?method=flickr.photos.search" +
            "&api_key=" + API_KEY +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s")
    Call<Flickr> searchFlickr(@Query("page") Integer page, @Query("text") @Nullable String text);

}
