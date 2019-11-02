package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.AlbumActivity;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.AlbumDiffCallback;
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.misc.Statics;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.lightenOrDarkenColorUntilTheyAreContrasty;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getSongsOfAlbum;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Album> albums;
    private int pixels;
    private Activity activity;
    private int albumGrid;
    private int albumItemStyle;

    private int colorAccent;
    private int contrastColorAccent;
    private int windowBackgroundColor;
    private int textColorPrimary;
    private int cardBackgroundColor;

    private RequestOptions options;

    private int imageType;

    public AlbumAdapter(ArrayList<Album> albums, Activity activity) {
        try {
            this.albums = new ArrayList<>(albums);
            this.activity = activity;
            this.albumGrid = SymphonyApplication.getInstance().getPreferenceUtils().getAlbumGrid();
            this.albumItemStyle = SymphonyApplication.getInstance().getPreferenceUtils().getAlbumItemStyle();
            this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
            if (activity != null) {
                if (albumGrid == 1) {
                    pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
                } else {
                    pixels = (activity.getResources().getDisplayMetrics().widthPixels / albumGrid);
                }
            }
            options = new RequestOptions();
            options = options.override(pixels, pixels);
            options = options.error(R.drawable.ic_blank_album_art);
            if (activity != null) {
                colorAccent = ThemeUtils.getThemeAccentColor(activity);
                contrastColorAccent = ContrastColor(colorAccent);
                windowBackgroundColor = ThemeUtils.getThemeWindowBackgroundColor(activity);

                if (ContrastColor(windowBackgroundColor) == Color.BLACK) {
                    cardBackgroundColor = ContextCompat.getColor(activity, R.color.md_grey_100);
                } else {
                    cardBackgroundColor = ContextCompat.getColor(activity, R.color.md_grey_900);
                }
                textColorPrimary = ThemeUtils.getThemeTextColorPrimary(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(albumGrid == 1 ? ((albumItemStyle == 0 || albumItemStyle == 4) ? R.layout.album_item_gridsize_1 : R.layout.album_item_gridsize_1_card) : (albumItemStyle == 0 ? R.layout.album_item : (albumItemStyle == 4) ? R.layout.album_item_circle : R.layout.album_item_card), parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            colorViewHolder(holder, new PaletteBitmap(null, colorAccent, contrastColorAccent));

            final Album currentAlbum = albums.get(position);
            holder.albumArt.setImageDrawable(null);

            holder.albumName.setText(currentAlbum.getName());
            holder.albumArtist.setText(currentAlbum.getArtist());

            try {
                if (albumGrid == 1) {
                    if (albumItemStyle == 4 || imageType == 0) {
                        options = options.transforms(new CenterCrop(), new CircleCrop());
                    } else if (imageType == 1) {
                        options = options.transforms(new CenterCrop());
                    } else {
                        options = options.transforms(new CenterCrop(), new RoundedCorners(24));
                    }
                } else {
                    if (albumItemStyle == 4) {
                        options = options.transforms(new CenterCrop(), new CircleCrop());
                    } else {
                        options = options.transforms(new CenterCrop());
                    }
                }
                holder.target = GlideApp.with(activity)
                        .as(PaletteBitmap.class)
                        .apply(options)
                        .load(ContentUris.withAppendedId(Statics.artworkUri, currentAlbum.getId()))
                        .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                try {
                                    if (SymphonyApplication.getInstance().getPreferenceUtils().getColorizeElementsAccordingToAlbumArt()) {
                                        colorViewHolder(holder, resource);
                                    }
                                    if (resource != null && resource.bitmap != null) {
                                        holder.albumArt.setImageBitmap(resource.bitmap);
                                        holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });
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
        holder.itemView.clearAnimation();
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        try {
            return albums.get(i).getName().substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void changeAlbums(ArrayList<Album> albums) {
        if (albums == null) {
            return;
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AlbumDiffCallback(albums, this.albums));
        diffResult.dispatchUpdatesTo(this);
        this.albums = new ArrayList<>(albums);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.albumName)
        public TextView albumName;
        @BindView(R.id.albumArtist)
        public TextView albumArtist;
        @BindView(R.id.menu)
        public ImageButton menu;
        @Nullable
        @BindView(R.id.card)
        MaterialCardView card;

        Target<PaletteBitmap> target;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnLongClick({R.id.albumItemOnClicker})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.albumItemOnClicker, R.id.menu})
        void OnClick(View view) {
            final Album album = albums.get(ViewHolder.this.getAdapterPosition());
            switch (view.getId()) {
                case R.id.albumItemOnClicker: {
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
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_albums, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final ArrayList<Song> songs = getSongsOfAlbum(album.getId(), activity.getContentResolver());
                        switch (item.getItemId()) {
                            case R.id.action_play: {
                                playList(songs, 0);
                                break;
                            }
                            case R.id.action_add_to_queue: {
                                addListToQueue(songs);
                                break;
                            }
                            case R.id.action_add_to_playlist: {
                                showAddToPlaylistDialog(activity, songs);
                                break;
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                    break;
                }
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    private void colorViewHolder(ViewHolder holder, PaletteBitmap resource) {
        if (resource != null) {
            switch (albumItemStyle) {
                case 0: {
                    setTextColor(textColorPrimary, holder.albumName, holder.albumArtist);
                    setColorFilter(textColorPrimary, holder.menu);
                    break;
                }
                case 1: {
                    setTextColor(textColorPrimary, holder.albumName, holder.albumArtist);
                    setColorFilter(textColorPrimary, holder.menu);
                    if (holder.card != null) {
                        holder.card.setCardBackgroundColor(cardBackgroundColor);
                    }
                    break;
                }
                case 2: {
                    int strokeColor = (resource.backgroundColor == colorAccent) ? colorAccent : lightenOrDarkenColorUntilTheyAreContrasty(windowBackgroundColor, resource.backgroundColor);
                    if (holder.card != null) {
                        holder.card.setStrokeWidth((int) activity.getResources().getDisplayMetrics().density);
                        holder.card.setStrokeColor(strokeColor);
                        holder.card.setCardElevation(0);
                    }
                    setTextColor(textColorPrimary, holder.albumName, holder.albumArtist);
                    setColorFilter(textColorPrimary, holder.menu);
                    break;
                }
                case 3: {
                    if (holder.card != null) {
                        holder.card.setCardBackgroundColor(resource.backgroundColor);
                    }
                    setTextColor(resource.foregroundColor, holder.albumName, holder.albumArtist);
                    setColorFilter(resource.foregroundColor, holder.menu);
                    break;
                }
                case 4: {
                    setTextColor(textColorPrimary, holder.albumName, holder.albumArtist);
                    setColorFilter(textColorPrimary, holder.menu);
                    break;
                }
            }
        }
    }
}
