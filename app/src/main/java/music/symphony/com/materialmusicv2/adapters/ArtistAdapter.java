package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
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
import music.symphony.com.materialmusicv2.activities.ArtistActivity;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.ArtistDiffCallback;
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Artist;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.lightenOrDarkenColorUntilTheyAreContrasty;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getSongsOfArtist;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Artist> artists;
    private int pixels;
    private Activity activity;
    private int artistGrid;
    private int artistItemStyle;

    private int colorAccent;
    private int contrastColorAccent;
    private int windowBackgroundColor;
    private int textColorPrimary;
    private int cardBackgroundColor;

    private RequestOptions options;

    public ArtistAdapter(ArrayList<Artist> artists, Activity activity) {
        try {
            this.artists = new ArrayList<>(artists);
            this.activity = activity;
            this.artistGrid = SymphonyApplication.getInstance().getPreferenceUtils().getArtistGrid();
            this.artistItemStyle = SymphonyApplication.getInstance().getPreferenceUtils().getArtistItemStyle();
            int imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
            if (activity != null) {
                if (artistGrid == 1) {
                    pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
                } else {
                    pixels = (activity.getResources().getDisplayMetrics().widthPixels / 2);
                }
            }
            options = new RequestOptions();
            options = options.override(pixels, pixels);
            if (artistGrid == 1) {
                if (artistItemStyle == 4 || imageType == 0) {
                    options = options.transforms(new CenterCrop(), new CircleCrop());
                } else if (imageType == 1) {
                    options = options.transforms(new CenterCrop());
                } else {
                    options = options.transforms(new CenterCrop(), new RoundedCorners(24));
                }
            } else {
                if (artistItemStyle == 4) {
                    options = options.transforms(new CenterCrop(), new CircleCrop());
                } else {
                    options = options.transforms(new CenterCrop());
                }
            }
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(artistGrid == 1 ? ((artistItemStyle == 0 || artistItemStyle == 4) ? R.layout.artist_item_gridsize_1 : R.layout.artist_item_gridsize_1_card) : (artistItemStyle == 0 ? R.layout.artist_item : (artistItemStyle == 4) ? R.layout.artist_item_circle : R.layout.artist_item_card), parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            colorViewHolder(holder, new PaletteBitmap(null, colorAccent, contrastColorAccent));

            final Artist currentArtist = artists.get(position);

            holder.target = GlideApp.with(activity)
                    .as(PaletteBitmap.class)
                    .error(R.drawable.ic_blank_album_art)
                    .apply(options)
                    .load(currentArtist)
                    .into(new ImageViewTarget<PaletteBitmap>(holder.artistImage) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            try {
                                if (SymphonyApplication.getInstance().getPreferenceUtils().getColorizeElementsAccordingToAlbumArt()) {
                                    colorViewHolder(holder, resource);
                                }
                                if (resource != null && resource.bitmap != null) {
                                    holder.artistImage.setImageBitmap(resource.bitmap);
                                    holder.artistImage.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });

            holder.artistName.setText(currentArtist.getName());
            holder.artistSongDetails.setText(String.format(activity.getString(R.string.artist_song_details_placeholder), String.valueOf(currentArtist.getNumberOfAlbums()), String.valueOf(currentArtist.getNumberOfTracks())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return artists.size();
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
        viewHolder.artistImage.clearAnimation();

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
            return artists.get(i).getName().substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.artistImage)
        ImageView artistImage;
        @BindView(R.id.artistName)
        TextView artistName;
        @BindView(R.id.artistSongDetails)
        TextView artistSongDetails;
        @BindView(R.id.menu)
        ImageButton menu;
        @Nullable
        @BindView(R.id.card)
        MaterialCardView card;

        Target<PaletteBitmap> target;

        @OnLongClick({R.id.artistItemOnClicker})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.artistItemOnClicker, R.id.menu})
        public void onClick(View view) {
            final Artist artist = artists.get(ViewHolder.this.getAdapterPosition());
            switch (view.getId()) {
                case R.id.artistItemOnClicker: {
                    Intent intent = new Intent(activity, ArtistActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ARTIST_TITLE", artist.getName());
                    bundle.putLong("ARTIST_ID", artist.getId());
                    intent.putExtras(bundle);
                    Bundle options = ActivityOptionsCompat.makeCustomAnimation(activity,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    activity.startActivity(intent, options);
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_artists, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (activity == null) {
                            return false;
                        }
                        final ArrayList<Song> songs = getSongsOfArtist(activity.getContentResolver(), MediaStore.Audio.Media.TITLE, artist.getName());
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

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void changeArtists(ArrayList<Artist> artists) {
        if (artists == null) {
            return;
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ArtistDiffCallback(artists, this.artists));
        diffResult.dispatchUpdatesTo(this);
        this.artists = new ArrayList<>(artists);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    private void colorViewHolder(ViewHolder holder, PaletteBitmap resource) {
        if (resource != null) {
            switch (artistItemStyle) {
                case 0: {
                    setTextColor(textColorPrimary, holder.artistName, holder.artistSongDetails);
                    setColorFilter(textColorPrimary, holder.menu);
                    break;
                }
                case 1: {
                    setTextColor(textColorPrimary, holder.artistName, holder.artistSongDetails);
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
                    setTextColor(textColorPrimary, holder.artistName, holder.artistSongDetails);
                    setColorFilter(textColorPrimary, holder.menu);
                    break;
                }
                case 3: {
                    if (holder.card != null) {
                        holder.card.setCardBackgroundColor(resource.backgroundColor);
                    }
                    setTextColor(resource.foregroundColor, holder.artistName, holder.artistSongDetails);
                    setColorFilter(resource.foregroundColor, holder.menu);
                    break;
                }
            }
        }
    }
}
