package music.symphony.com.materialmusicv2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Song;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class NowPlayingBackgroundAdapter extends PagerAdapter {

    private ArrayList<Song> songs;
    private Context context;
    private LayoutInflater layoutInflater;
    private int screenHeight;
    private int screenWidth;

    public NowPlayingBackgroundAdapter(Context context, ArrayList<Song> songs) {
        try {
            if (songs == null) {
                this.songs = new ArrayList<>();
            } else {
                this.songs = new ArrayList<>(songs);
            }
            this.context = context;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        int nowPlayingStyle = SymphonyApplication.getInstance().getPreferenceUtils().getNowPlayingStyle();
        View itemView = layoutInflater.inflate((nowPlayingStyle == 1 || nowPlayingStyle == 3 || nowPlayingStyle == 4) ? R.layout.now_playing_background_image_square : R.layout.now_playing_background_image, container, false);
        final ImageView albumArt = itemView.findViewById(R.id.albumArt);
        Song currentSong = songs.get(position);

        try {
            GlideApp.with(context)
                    .load(currentSong)
                    .override(screenWidth, screenHeight)
                    .centerCrop()
                    .fitCenter()
                    .transition(withCrossFade())
                    .error(
                            GlideApp
                                    .with(context)
                                    .load(R.drawable.ic_blank_album_art)
                                    .override(screenWidth, screenHeight)
                                    .centerCrop()
                                    .fitCenter()
                                    .transition(withCrossFade())
                    )
                    .into(albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, final int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }

}