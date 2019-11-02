package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.GenrePlaylistActivity;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.GenreDiffCallback;
import music.symphony.com.materialmusicv2.objects.Genre;
import music.symphony.com.materialmusicv2.objects.Song;

import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getAllSongsFromGenre;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Genre> genres;
    private Activity activity;

    public GenreAdapter(ArrayList<Genre> genres, Activity activity) {
        this.genres = new ArrayList<>(genres);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.genre_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            final Genre currentGenre = genres.get(position);
            holder.genreName.setText(currentGenre.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return genres.size();
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
            return genres.get(i).getName().substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.genreName)
        TextView genreName;
        @BindView(R.id.genreItemBackground)
        RelativeLayout genreItemBackground;
        @BindView(R.id.menu)
        ImageButton menu;

        @OnLongClick({R.id.genreItemBackground})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.genreItemBackground, R.id.menu})
        public void onClick(View view) {
            final Genre genre = genres.get(ViewHolder.this.getAdapterPosition());
            switch (view.getId()) {
                case R.id.genreItemBackground: {
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(activity, GenrePlaylistActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("TITLE", genre.getName());
                        bundle.putLong("ID", genre.getId());
                        bundle.putInt("WHATTODO", 0);
                        bundle.putString("PATH", null);
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                    }, 250);
                    break;
                }
                case R.id.menu: {
                    if (activity == null) {
                        return;
                    }
                    final ArrayList<Song> songs = getAllSongsFromGenre(activity.getContentResolver(), MediaStore.Audio.Media.TITLE, genre.getId());
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_genres, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
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

    public void changeGenres(ArrayList<Genre> genres) {
        if (genres == null) {
            return;
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new GenreDiffCallback(genres, this.genres));
        diffResult.dispatchUpdatesTo(this);
        this.genres = new ArrayList<>(genres);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }
}