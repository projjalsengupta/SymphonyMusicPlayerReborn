package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.AlbumActivity;
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.utils.misc.Statics;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;

public class AlbumAdapterForArtists extends RecyclerView.Adapter<AlbumAdapterForArtists.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Album> albums;
    private Activity activity;

    private int colorAccent;
    private int contrastColorAccent;

    private RequestOptions options;

    public AlbumAdapterForArtists(ArrayList<Album> albums, Activity activity) {
        try {
            this.albums = new ArrayList<>(albums);
            this.activity = activity;
            int pixels = (int) (100 * activity.getResources().getDisplayMetrics().density);
            options = new RequestOptions();
            options = options.override(pixels, pixels);
            options = options.error(R.drawable.ic_blank_album_art);
            colorAccent = ThemeUtils.getThemeAccentColor(activity);
            contrastColorAccent = ContrastColor(colorAccent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_for_artist_page, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            colorViewHolder(holder, new PaletteBitmap(null, colorAccent, contrastColorAccent));

            final Album currentAlbum = albums.get(position);

            holder.albumName.setText(currentAlbum.getName());
            String albumYearString = String.valueOf(currentAlbum.getYear());
            if (albumYearString.equals("0")) {
                albumYearString = "-";
            }
            holder.albumYear.setText(albumYearString);

            holder.target = GlideApp.with(activity)
                    .as(PaletteBitmap.class)
                    .apply(options)
                    .load(ContentUris.withAppendedId(Statics.artworkUri, currentAlbum.getId()))
                    .centerCrop()
                    .fitCenter()
                    .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            colorViewHolder(holder, resource);
                            if (resource != null && resource.bitmap != null) {
                                holder.albumArt.setImageBitmap(resource.bitmap);
                                holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder viewHolder) {
        if (viewHolder.target != null) {
            try {
                GlideApp.with(activity).clear(viewHolder.target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        viewHolder.albumArt.clearAnimation();

        super.onViewRecycled(viewHolder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        try {
            return albums.get(i).getName().substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.albumName)
        TextView albumName;
        @BindView(R.id.albumYear)
        TextView albumYear;
        @BindView(R.id.albumItemBackground)
        RelativeLayout albumItemBackground;

        Target<PaletteBitmap> target;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.albumItemOnClicker})
        void OnClick(View view) {
            final Album album = albums.get(ViewHolder.this.getAdapterPosition());
            if (view.getId() == R.id.albumItemOnClicker) {
                new Handler().postDelayed(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("ALBUM_NAME", album.getName());
                    bundle.putString("ARTIST_NAME", album.getArtist());
                    bundle.putLong("ALBUM_ID", album.getId());
                    bundle.putInt("ALBUM_YEAR", album.getYear());
                    Intent intent = new Intent(activity, AlbumActivity.class);
                    intent.putExtras(bundle);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(activity, albumArt, albumArt.getTransitionName());
                        activity.startActivity(intent, options.toBundle());
                    } else {
                        activity.startActivity(intent);
                    }
                }, 250);
            }
        }
    }

    private void colorViewHolder(ViewHolder holder, PaletteBitmap resource) {
        if (resource != null) {
            holder.albumItemBackground.setBackgroundColor(resource.backgroundColor);
            holder.albumName.setTextColor(resource.foregroundColor);
            holder.albumYear.setTextColor(resource.foregroundColor);
        }
    }
}
