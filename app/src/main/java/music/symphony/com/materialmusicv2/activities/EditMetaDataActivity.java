package music.symphony.com.materialmusicv2.activities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.customviews.others.SquareImageView;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileStatics;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.MediaFile;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.misc.Statics;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.changeTextInputLayoutHintColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.fileutils.FileUtils.copyFileToExternalSDCardForGreaterThanKitkat;
import static music.symphony.com.materialmusicv2.utils.fileutils.FileUtils.copyFileToExternalSDCardForKitkat;
import static music.symphony.com.materialmusicv2.utils.fileutils.FileUtils.deleteCache;
import static music.symphony.com.materialmusicv2.utils.fileutils.FileUtils.saveToInternalStorage;
import static music.symphony.com.materialmusicv2.utils.lyricsutils.LyricsUtils.deleteLyricsFromCache;
import static music.symphony.com.materialmusicv2.utils.lyricsutils.LyricsUtils.getLyricsFromCache;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class EditMetaDataActivity extends AppCompatActivity implements MediaScannerConnection.MediaScannerConnectionClient {

    @BindView(R.id.trackNameLayout)
    TextInputLayout trackNameLayout;
    @BindView(R.id.albumNameLayout)
    TextInputLayout albumNameLayout;
    @BindView(R.id.artistNameLayout)
    TextInputLayout artistNameLayout;
    @BindView(R.id.albumArtistNameLayout)
    TextInputLayout albumArtistNameLayout;
    @BindView(R.id.yearLayout)
    TextInputLayout yearLayout;
    @BindView(R.id.genreLayout)
    TextInputLayout genreLayout;
    @BindView(R.id.lyricsLayout)
    TextInputLayout lyricsLayout;
    @BindView(R.id.trackName)
    TextInputEditText trackName;
    @BindView(R.id.albumName)
    TextInputEditText albumName;
    @BindView(R.id.artistName)
    TextInputEditText artistName;
    @BindView(R.id.albumArtistName)
    TextInputEditText albumArtistName;
    @BindView(R.id.year)
    TextInputEditText year;
    @BindView(R.id.genre)
    TextInputEditText genre;
    @BindView(R.id.lyrics)
    TextInputEditText lyrics;
    @BindView(R.id.fab)
    FloatingActionButton editAlbumArt;
    @BindView(R.id.save)
    ExtendedFloatingActionButton save;
    @BindView(R.id.albumArt)
    SquareImageView albumArt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @OnClick({R.id.save, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save: {
                deleteLyricsFromCache(getApplicationContext(), ID);
                deleteCache(getApplicationContext());
                if (trackName != null && albumName != null && artistName != null && albumArtistName != null && year != null && genre != null && lyrics != null) {
                    editMetaData(Objects.requireNonNull(trackName.getText()).toString(),
                            Objects.requireNonNull(albumName.getText()).toString(),
                            Objects.requireNonNull(artistName.getText()).toString(),
                            Objects.requireNonNull(albumArtistName.getText()).toString(),
                            Objects.requireNonNull(year.getText()).toString(),
                            Objects.requireNonNull(genre.getText()).toString(),
                            Objects.requireNonNull(lyrics.getText()).toString());
                }
                break;
            }
            case R.id.fab: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;
            }
        }
    }

    private Long ID = 0L;
    private Bitmap bitmap = null;
    private Artwork artwork;
    private Tag tag = null;
    private File audio;
    private String path;
    private AudioFile f;
    private File file;

    private MediaScannerConnection mediaScannerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_edit_meta_data);

        ButterKnife.bind(this);

        int colorPrimary = getThemePrimaryColor(this);
        int colorAccent = getThemeAccentColor(this);
        int contrastColor = ContrastColor(colorPrimary);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(EditMetaDataActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        ToolbarUtils.setUpToolbar(
                toolbar,
                getString(R.string.edit_metadata),
                new int[]{
                        R.drawable.ic_arrow_back_black_24dp,
                        R.drawable.ic_arrow_back_white_24dp
                },
                getThemeWindowBackgroundColor(EditMetaDataActivity.this),
                EditMetaDataActivity.this,
                this::onBackPressed,
                true
        );

        setColorFilter(ContrastColor(colorAccent), editAlbumArt);
        setColorFilter(ContrastColor(colorAccent), save);

        ID = getIntent().getLongExtra("ID", 0L);
        path = getIntent().getStringExtra("PATH");

        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setCollapsedTitleTextColor(contrastColor);
            collapsingToolbarLayout.setBackgroundColor(colorPrimary);
            collapsingToolbarLayout.setStatusBarScrimColor(colorPrimary);
            collapsingToolbarLayout.setContentScrimColor(colorPrimary);
        }

        changeTextInputLayoutHintColor(colorAccent,
                trackNameLayout,
                albumNameLayout,
                artistNameLayout,
                albumArtistNameLayout,
                yearLayout,
                genreLayout,
                lyricsLayout
        );

        init();
    }

    public void SingleMediaScanner(Context context, File file) {
        this.file = file;
        mediaScannerConnection = new MediaScannerConnection(context, this);
        mediaScannerConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mediaScannerConnection.scanFile(file.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mediaScannerConnection.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (albumArt != null) {
                    albumArt.setImageURI(uri);
                }
                ParcelFileDescriptor parcelFileDescriptor = null;
                try {
                    if (uri != null) {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    }
                    if (parcelFileDescriptor != null) {
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        bitmap = BitmapUtils.resizeBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor), 512, 512);
                        if (bitmap != null) {
                            saveToInternalStorage(bitmap, getApplicationContext());
                        }
                        parcelFileDescriptor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                    File directory = contextWrapper.getDir("SymphonyCache", Context.MODE_PRIVATE);
                    File cacheFile = new File(directory, "cache.jpg");
                    artwork = ArtworkFactory.createArtworkFromFile(cacheFile);
                } catch (Exception e) {
                    postToast(R.string.cant_set_this_artwork, getApplicationContext(), TOAST_ERROR);
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Statics.treeUri = data.getData();
            final SharedPreferences.Editor editor = getSharedPreferences("mypref", MODE_PRIVATE).edit();
            editor.putString("treeUri", Statics.treeUri.toString());
            editor.apply();
            grantUriPermission(getPackageName(), Statics.treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(Statics.treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            save.callOnClick();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
        sendBroadcast(intent);
    }

    private void scanMediaAfterEdited() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("SymphonyLyrics", Context.MODE_PRIVATE);
        File myFile = new File(directory, ID + ".txt");
        if (myFile.exists()) {
            if (myFile.delete()) {
                Log.v("isDeleted", "yes");
            }
        }
        SingleMediaScanner(getApplicationContext(), audio);
    }

    private void changeTagsBeforeWriting(String... str) {
        Tag newTag;
        newTag = f.getTag();
        if (newTag != null) {
            try {
                newTag.setField(FieldKey.TITLE, str[0]);
                newTag.setField(FieldKey.ALBUM, str[1]);
                newTag.setField(FieldKey.ARTIST, str[2]);
                newTag.setField(FieldKey.ALBUM_ARTIST, str[3]);
                newTag.setField(FieldKey.YEAR, str[4]);
                newTag.setField(FieldKey.GENRE, str[5]);
                newTag.setField(FieldKey.LYRICS, str[6]);
                newTag.deleteArtworkField();
                newTag.addField(artwork);
                newTag.setField(artwork);
            } catch (Exception e) {
                e.printStackTrace();
            }
            f.setTag(newTag);
        }
    }

    private void init() {
        AsyncTask.execute(() -> {
            int pixels = getResources().getDisplayMetrics().widthPixels;
            audio = new File(path);
            f = null;
            try {
                f = AudioFileIO.read(audio);
                artwork = f.getTag().getFirstArtwork();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (f != null) {
                if (artwork != null) {
                    byte[] data;
                    data = artwork.getBinaryData();
                    if (data != null) {
                        bitmap = BitmapUtils.decodeByteArray(data, data.length, pixels, pixels, Bitmap.Config.ARGB_8888);
                    } else {
                        bitmap = BitmapUtils.decodeResource(getResources(), R.drawable.ic_blank_album_art, pixels, pixels, Bitmap.Config.ARGB_8888);
                    }
                } else {
                    bitmap = BitmapUtils.decodeResource(getResources(), R.drawable.ic_blank_album_art, pixels, pixels, Bitmap.Config.ARGB_8888);
                }
                try {
                    tag = f.getTag();
                } catch (Exception e) {
                    e.printStackTrace();
                    tag = null;
                }
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                if (tag != null) {
                    if (trackName != null) {
                        trackName.setText(tag.getFirst(FieldKey.TITLE));
                    }
                    if (albumName != null) {
                        albumName.setText(tag.getFirst(FieldKey.ALBUM));
                    }
                    if (artistName != null) {
                        artistName.setText(tag.getFirst(FieldKey.ARTIST));
                    }
                    if (albumArtistName != null) {
                        albumArtistName.setText(tag.getFirst(FieldKey.ALBUM_ARTIST));
                    }
                    if (year != null) {
                        year.setText(tag.getFirst(FieldKey.YEAR));
                    }
                    if (genre != null) {
                        genre.setText(tag.getFirst(FieldKey.GENRE));
                    }
                    String lyricsText = getLyricsFromCache(getApplicationContext(), ID);
                    if (lyricsText.equals(getString(R.string.no_lyrics))) {
                        lyricsText = tag.getFirst(FieldKey.LYRICS);
                    }
                    if (lyrics != null) {
                        lyrics.setText(lyricsText);
                    }
                    if (albumArt != null) {
                        albumArt.setImageBitmap(bitmap);
                    }
                } else {
                    postToast(R.string.cant_edit_this_song, getApplicationContext(), TOAST_ERROR);
                    finish();
                }
            });
        });
    }

    private void editMetaData(final String... str) {
        AsyncTask.execute(() -> {
            String resultString;
            try {
                changeTagsBeforeWriting(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (f != null) {
                try {
                    AudioFileIO.write(f);
                    resultString = getString(R.string.successfully_edited_song_scanned);
                } catch (Exception e) {
                    e.printStackTrace();
                    resultString = getString(R.string.error_label);
                }
            } else {
                resultString = getString(R.string.error_label);
            }
            final String finalB = resultString;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalB.equals(getString(R.string.successfully_edited_song_scanned))) {
                    try {
                        scanMediaAfterEdited();
                        postToast(finalB, getApplicationContext(), TOAST_SUCCESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (Statics.getTreeUri(EditMetaDataActivity.this) != null) {
                        editSongOnExternalSDCard(Objects.requireNonNull(trackName.getText()).toString(),
                                Objects.requireNonNull(albumName.getText()).toString(),
                                Objects.requireNonNull(artistName.getText()).toString(),
                                Objects.requireNonNull(albumArtistName.getText()).toString(),
                                Objects.requireNonNull(year.getText()).toString(),
                                Objects.requireNonNull(genre.getText()).toString(),
                                Objects.requireNonNull(lyrics.getText()).toString());
                    }
                }
            });
        });
    }

    private void editSongOnExternalSDCard(final String... str) {
        AsyncTask.execute(() -> {
            String resultString = "";
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DocumentFile documentFile = FileStatics.getDocumentFileIfAllowedToWrite(file, getApplicationContext());
                if (documentFile != null) {
                    File createdFile = FileUtils.copyFileToCacheSpace(file.getPath(), file.getName(), getApplicationContext());
                    if (createdFile != null && createdFile.exists()) {
                        try {
                            f = AudioFileIO.read(createdFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            changeTagsBeforeWriting(str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            AudioFileIO.write(f);
                        } catch (Exception e) {
                            e.printStackTrace();
                            resultString = getString(R.string.error_label);
                        }
                        if (!resultString.equals(getString(R.string.error_label))) {
                            if (copyFileToExternalSDCardForGreaterThanKitkat(file, file.getParentFile(), createdFile.getPath(), createdFile.getName(), getApplicationContext())) {
                                audio = file;
                                createdFile.delete();
                                resultString = getString(R.string.successfully_edited_song_scanned);
                            } else {
                                resultString = getString(R.string.cant_edit_this_song);
                            }
                        }
                    } else {
                        resultString = getString(R.string.cant_edit_this_song);
                    }
                } else {
                    resultString = getString(R.string.cant_edit_this_song);
                }
            } else {
                MediaFile mediaFile = new MediaFile(getContentResolver(), file);
                if (mediaFile.getFile().exists()) {
                    File createdFile = FileUtils.copyFileToCacheSpace(file.getPath(), file.getName(), getApplicationContext());
                    if (createdFile != null && createdFile.exists()) {
                        try {
                            f = AudioFileIO.read(createdFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            changeTagsBeforeWriting(str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            AudioFileIO.write(f);
                        } catch (Exception e) {
                            e.printStackTrace();
                            resultString = getString(R.string.error_label);
                        }
                        try {
                            mediaFile.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!resultString.equals(getString(R.string.error_label))) {
                            if (copyFileToExternalSDCardForKitkat(createdFile.getPath(), mediaFile)) {
                                audio = file;
                                createdFile.delete();
                                resultString = getString(R.string.successfully_edited_song_scanned);
                            } else {
                                resultString = getString(R.string.cant_edit_this_song);
                            }
                        }
                    } else {
                        resultString = getString(R.string.cant_edit_this_song);
                    }
                } else {
                    resultString = getString(R.string.cant_edit_this_song);
                }
            }
            final String finalB = resultString;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalB.equals(getString(R.string.successfully_edited_song_scanned))) {
                    try {
                        scanMediaAfterEdited();
                        postToast(finalB, getApplicationContext(), TOAST_SUCCESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    postToast(finalB, getApplicationContext(), TOAST_INFO);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mediaScannerConnection != null) {
                mediaScannerConnection.disconnect();
                mediaScannerConnection = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}