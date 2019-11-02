package music.symphony.com.materialmusicv2.activities;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.adapters.SearchAdapter;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Artist;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;

public class SearchActivity extends AppCompatActivity implements TextWatcher {

    @BindView(R.id.back)
    ImageButton back;
    @BindView(R.id.search)
    EditText search;
    @BindView(R.id.toolbar_card)
    MaterialCardView cardView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    ExtendedFloatingActionButton fab;

    private ArrayList<Song> songs;
    private ArrayList<Album> albums;
    private ArrayList<Artist> artists;

    private SearchAdapter searchAdapter;

    @OnClick({R.id.back, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back: {
                onBackPressed();
                break;
            }
            case R.id.fab: {
                if (search == null) {
                    return;
                }
                search.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        ColorUtils.setColorFilter(ThemeUtils.getThemeTextColorPrimary(SearchActivity.this), back);
        cardView.setCardBackgroundColor(ThemeUtils.getThemeAccentColor(SearchActivity.this));
        search.setTextColor(ContrastColor(ThemeUtils.getThemeAccentColor(SearchActivity.this)));
        search.setHintTextColor(ContrastColor(ThemeUtils.getThemeAccentColor(SearchActivity.this)));
        setColorFilter(ContrastColor(ThemeUtils.getThemeAccentColor(SearchActivity.this)), back);
        search.addTextChangedListener(SearchActivity.this);
        search.requestFocus();

        songs = QueryUtils.getAllSongs(getContentResolver(), MediaStore.Audio.Media.TITLE);
        albums = QueryUtils.getAllAlbums(getContentResolver());
        artists = QueryUtils.getAllArtists(getContentResolver());

        setUpRecyclerView(recyclerView, new LinearLayoutManager(SearchActivity.this), getThemeAccentColor(SearchActivity.this));

        if (fab != null) {
            setColorFilter(ContrastColor(getThemeAccentColor(SearchActivity.this)), fab);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    if (fab != null) {
                        fab.shrink();
                    }
                }
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (fab != null) {
                        fab.extend();
                    }
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public void search(String query) {
        if (query == null || query.equals("")) {
            if (searchAdapter == null) {
                searchAdapter = new SearchAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), SearchActivity.this);
                recyclerView.setAdapter(searchAdapter);
            } else {
                searchAdapter.update(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }
            return;
        }
        recyclerView.setVisibility(View.VISIBLE);
        final ArrayList<Song> filteredSongList = filterSongs(songs, query);
        final ArrayList<Album> filteredAlbumList = filterAlbums(albums, query);
        final ArrayList<Artist> filteredArtistList = filterArtists(artists, query);
        if (searchAdapter == null) {
            searchAdapter = new SearchAdapter(filteredSongList, filteredAlbumList, filteredArtistList, SearchActivity.this);
            recyclerView.setAdapter(searchAdapter);
        } else {
            searchAdapter.update(filteredSongList, filteredAlbumList, filteredArtistList);
        }

    }

    private ArrayList<Song> filterSongs(ArrayList<Song> songs, String query) {
        if (songs == null || query == null) {
            return new ArrayList<>();
        }
        ArrayList<Song> filteredList = new ArrayList<>();
        query = query.toLowerCase();
        for (Song song : songs) {
            if (song != null) {
                final String name = song.getName().toLowerCase();
                if (name.contains(query)) {
                    filteredList.add(song);
                }
            }
        }
        return filteredList;
    }

    private ArrayList<Album> filterAlbums(ArrayList<Album> albums, String query) {
        if (albums == null || query == null) {
            return new ArrayList<>();
        }
        ArrayList<Album> filteredList = new ArrayList<>();
        query = query.toLowerCase();
        for (Album album : albums) {
            if (album != null) {
                final String name = album.getName().toLowerCase();
                if (name.contains(query)) {
                    filteredList.add(album);
                }
            }
        }
        return filteredList;
    }

    private ArrayList<Artist> filterArtists(ArrayList<Artist> artists, String query) {
        if (artists == null || query == null) {
            return new ArrayList<>();
        }
        ArrayList<Artist> filteredList = new ArrayList<>();
        query = query.toLowerCase();
        for (Artist artist : artists) {
            if (artist != null) {
                final String name = artist.getName().toLowerCase();
                if (name.contains(query)) {
                    filteredList.add(artist);
                }
            }
        }
        return filteredList;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        search(charSequence.toString());
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        search(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        SearchActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
