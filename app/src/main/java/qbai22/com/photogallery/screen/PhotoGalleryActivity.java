package qbai22.com.photogallery.screen;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
