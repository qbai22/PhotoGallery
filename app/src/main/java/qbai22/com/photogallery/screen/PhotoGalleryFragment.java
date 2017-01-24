package qbai22.com.photogallery.screen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import qbai22.com.photogallery.R;
import qbai22.com.photogallery.model.Flickr;
import qbai22.com.photogallery.model.GalleryItem;
import qbai22.com.photogallery.network.ApiFactory;
import qbai22.com.photogallery.network.FlickrService;
import qbai22.com.photogallery.service.PollService;
import qbai22.com.photogallery.utils.QueryPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";

    private final int pollJobId = 1;
    private int standartColumns = 3;
    private int colWidth = 375;
    private int mPageNumber = 1;

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private PhotoAdapter mPhotoAdapter;
    private List<GalleryItem> mItems = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (QueryPreferences.getStoredQuery(getActivity()) == null) {
            fetchData(mPageNumber);
        } else {
            searchData(mPageNumber, QueryPreferences.getStoredQuery(getActivity()));
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem pollingItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            pollingItem.setTitle(R.string.stop_polling);
        } else {
            pollingItem.setTitle(R.string.start_polling);
        }

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e(TAG, "Query text submit" + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                mPhotoAdapter.clear();
                searchData(mPageNumber, query);
                //collapsing searchView and input keyboard after submitting query
                View v = getActivity().getCurrentFocus();
                if (v != null) {
                    searchView.clearFocus();
                    searchView.setQuery("", false);
                    searchView.setFocusable(false);
                    searchView.setIconified(true);
                    MenuItemCompat.collapseActionView(searchItem);
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchClickListener((v) -> {
            String query = QueryPreferences.getStoredQuery(getActivity());
            searchView.setQuery(query, false);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                mPhotoAdapter.clear();
                fetchData(mPageNumber);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm =
                        !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mLayoutManager = new GridLayoutManager(getActivity(), standartColumns);
        mRecyclerView = (RecyclerView) v
                .findViewById(R.id.recycler_view_fragment);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //calculating number of columns depending on screen size
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int colCount = Math.round(mRecyclerView.getWidth() / colWidth);
            if (colCount != standartColumns) {
                GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                layoutManager.setSpanCount(colCount);
            }
        });

        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                String q = QueryPreferences.getStoredQuery(getActivity());
                if (q == null) {
                    fetchData(page);
                } else {
                    searchData(page, q);
                }
            }
        });
        mPhotoAdapter = new PhotoAdapter(mItems);
        mRecyclerView.setAdapter(mPhotoAdapter);

        return v;
    }

    private void fetchData(int pageNumber) {

        FlickrService flickrService = ApiFactory.getFlickrService();

        Call<Flickr> call = flickrService.getPageFlickr(pageNumber);
        String s = flickrService.getPageFlickr(pageNumber).request().url().toString();
        Log.e(TAG, "onResponse:  " + s );
        call.enqueue(new Callback<Flickr>() {
            @Override
            public void onResponse(Call<Flickr> call, Response<Flickr> response) {
                    Log.e(TAG, "onResponse  " +  call.request().url().toString());
                    List<GalleryItem> responseList = response.body().photos.photo;
                    mItems.addAll(responseList);
                    mPhotoAdapter.notifyDataSetChanged();
                    QueryPreferences.setLastResultId(getActivity(),
                            mItems.get(mItems.size() - 1).getId());

            }

            @Override
            public void onFailure(Call<Flickr> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });
    }

    private void searchData(int pageNumber, String codeWord) {
        if (codeWord == null || codeWord.equals("")) {
            fetchData(mPageNumber);
            return;
        }
        QueryPreferences.setStoredQuery(getActivity(), codeWord); // saving query for service calls

        FlickrService flickrService = ApiFactory.getFlickrService();
        Call<Flickr> call = flickrService.searchFlickr(pageNumber, codeWord);

        call.enqueue(new Callback<Flickr>() {
            @Override
            public void onResponse(Call<Flickr> call, Response<Flickr> response) {
                Log.e(TAG, "onResponse  " +  call.request().url().toString());
                mItems.addAll(response.body().photos.photo);
                mPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Flickr> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private ImageView mImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView
                    .findViewById(R.id.item_gallery_list_view);
            itemView.setOnClickListener(this);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            Intent i = PhotoPageActivity
                    .newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> list) {
            mGalleryItems = list;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mItems.get(position);
            holder.bindGalleryItem(item);
            Picasso.with(getActivity()).load(item.getUrl()).into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        public void clear() {
            int size = mGalleryItems.size();
            List<GalleryItem> templist = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                templist.add(mGalleryItems.get(i));
            }
            mGalleryItems.removeAll(templist);
            this.notifyItemRangeRemoved(0, size);
        }
    }
}
