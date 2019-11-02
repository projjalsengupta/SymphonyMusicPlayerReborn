package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.AlbumActivity;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.utils.misc.Statics;

public class AlbumSongAlbumAdapter extends RecyclerView.Adapter<AlbumSongAlbumAdapter.ViewHolder> {
    private ArrayList<Album> albums;
    private Activity activity;

    private RequestOptions options;

    private int imageType;

    AlbumSongAlbumAdapter(ArrayList<Album> albums, Activity activity) {
        try {
            this.albums = new ArrayList<>(albums);
            this.activity = activity;
            this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
            int pixels = (int) (100 * activity.getResources().getDisplayMetrics().density);
            options = new RequestOptions();
            options = options.override(pixels, pixels);
            options = options.error(R.drawable.ic_blank_album_art);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_for_album_song_and_artist_page, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            final Album currentAlbum = albums.get(position);
            holder.albumArt.setImageDrawable(null);

            holder.albumName.setText(currentAlbum.getName());

            try {
                if (imageType == 0) {
                    options = options.transforms(new CenterCrop(), new CircleCrop());
                } else if (imageType == 1) {
                    options = options.transforms(new CenterCrop());
                } else {
                    options = options.transforms(new CenterCrop(), new RoundedCorners(24));
                }
                GlideApp.with(activity)
                        .load(ContentUris.withAppendedId(Statics.artworkUri, currentAlbum.getId()))
                        .apply(options)
                        .into(holder.albumArt);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        try {
            GlideApp.with(activity).clear(viewHolder.albumArt);
            viewHolder.albumArt.clearAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onViewRecycled(viewHolder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.albumName)
        TextView albumName;
        @BindView(R.id.albumItemOnClicker)
        LinearLayout albumItemOnClicker;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            albumItemOnClicker.setOnClickListener(view1 -> {
                final Album album = albums.get(ViewHolder.this.getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putString("ALBUM_NAME", album.getName());
                bundle.putString("ARTIST_NAME", album.getArtist());
                bundle.putLong("ALBUM_ID", album.getId());
                bundle.putInt("ALBUM_YEAR", album.getYear());
                Intent intent = new Intent(activity, AlbumActivity.class);
                intent.putExtras(bundle);
                Bundle options = ActivityOptionsCompat.makeCustomAnimation(activity,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                activity.startActivity(intent, options);
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }
}
