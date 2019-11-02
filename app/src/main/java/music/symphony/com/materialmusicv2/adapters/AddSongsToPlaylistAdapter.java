package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Song;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AddSongsToPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Song> songs;
    private boolean[] isSongChecked;
    private int pixels;
    private Activity activity;
    private int imageType;

    public AddSongsToPlaylistAdapter(ArrayList<Song> songs, Activity activity) {
        this.songs = new ArrayList<>(songs);
        this.activity = activity;
        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }
        isSongChecked = new boolean[songs.size()];
        for (int i = 0; i < isSongChecked.length; i++) {
            isSongChecked[i] = false;
        }
        this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_songs_to_playlist_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        Song currentSong = songs.get(position);

        try {
            RequestOptions requestOptions = new RequestOptions();
            if (imageType == 0) {
                requestOptions = requestOptions.transforms(new CenterCrop(), new CircleCrop());
            } else if (imageType == 1) {
                requestOptions = requestOptions.transforms(new CenterCrop());
            } else {
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(24));
            }
            GlideApp.with(activity)
                    .load(currentSong)
                    .override(pixels, pixels)
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .error(
                            GlideApp
                                    .with(activity)
                                    .load(R.drawable.ic_blank_album_art)
                                    .override(pixels, pixels)
                                    .apply(requestOptions)
                                    .transition(withCrossFade())
                    )
                    .into(holder.albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.songName.setText(currentSong.getName());
        holder.songArtist.setText(currentSong.getArtist());

        holder.check.setChecked(isSongChecked[position]);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        try {
            GlideApp.with(activity).clear(holder.albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onViewRecycled(viewHolder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        if (i == 0) {
            i = 1;
        }
        if (songs != null && songs.get(i) != null && songs.get(i).getName() != null) {
            return songs.get(i).getName().substring(0, 1);
        }
        return "";
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songArtist)
        TextView songArtist;
        @BindView(R.id.check)
        CheckBox check;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            check.setOnCheckedChangeListener(this);
        }

        @OnClick({R.id.itemBackground})
        public void onClick(View view) {
            if (view.getId() == R.id.itemBackground) {
                if (check != null) {
                    if (check.isChecked()) {
                        check.setChecked(false);
                    } else {
                        check.setChecked(true);
                    }
                }
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            isSongChecked[getAdapterPosition()] = isChecked;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    public ArrayList<Song> getCheckedSongs() {
        ArrayList<Song> checkedSongs = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            if (isSongChecked[i]) {
                checkedSongs.add(songs.get(i));
            }
        }
        return checkedSongs;
    }
}