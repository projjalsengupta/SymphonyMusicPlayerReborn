package music.symphony.com.materialmusicv2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.NowPlayingActivity;
import music.symphony.com.materialmusicv2.activities.QueueActivity;
import music.symphony.com.materialmusicv2.adapters.FileManagerAdapter;
import music.symphony.com.materialmusicv2.customviews.library.LibraryBottomPlaybackController;
import music.symphony.com.materialmusicv2.objects.FileList;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;

public class FragmentFolders extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.breadcrumbsView)
    BreadcrumbsView breadcrumbsView;
    @BindView(R.id.libraryBottomPlaybackController)
    LibraryBottomPlaybackController libraryBottomPlaybackController;

    private FileManagerAdapter fileManagerAdapter;

    private String currentLocation = Environment.getExternalStorageDirectory().getPath();
    private String rootLocation = Environment.getExternalStorageDirectory().getPath();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_folders, container, false);

        ButterKnife.bind(this, rootView);

        setUpRecyclerView(recyclerView, new LinearLayoutManager(getActivity()), getThemeTextColorPrimary(getActivity()));

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);

        setUpBottomController();

        setBreadcrumbsView();

        loadTask(null, currentLocation);

        return rootView;
    }

    private void loadTask(final BreadcrumbItem nextItem, final String path) {
        final FileList list = FileList.newInstance(path);

        if (list != null) {
            if (fileManagerAdapter == null) {
                fileManagerAdapter = new FileManagerAdapter(getActivity(), currentLocation);
                recyclerView.setAdapter(fileManagerAdapter);
                fileManagerAdapter.setCallback(file -> {
                    if (getActivity() == null) {
                        return;
                    }
                    if (file.isDirectory()) {
                        BreadcrumbItem breadcrumbItem = BreadcrumbItem.createSimpleItem(file.toString());
                        currentLocation = getCurrentPath() + "/" + file.toString();
                        loadTask(breadcrumbItem, currentLocation);
                    } else if (file.isFile()) {
                        ArrayList<Song> songs = QueryUtils.getAllSongsOfFolderWithoutSelection(getActivity().getContentResolver(), MediaStore.Audio.Media.TITLE, getCurrentPath());
                        String path1 = getCurrentPath() + "/" + file.toString();
                        int index = -1;
                        for (int i = 0; i < songs.size(); i++) {
                            if (path1.equals(songs.get(i).getPath())) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            playList(songs, index);
                        } else {
                            postToast(R.string.cant_play_song, getActivity(), TOAST_ERROR);
                        }
                    }
                });
            }
            fileManagerAdapter.setFileList(list, currentLocation);
            fileManagerAdapter.notifyDataSetChanged();
            if (nextItem != null && breadcrumbsView != null) {
                breadcrumbsView.addItem(nextItem);
            }
        }
    }

    private void setBreadcrumbsView() {
        if (breadcrumbsView == null) {
            return;
        }

        breadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {
            @Override
            public void onNavigateBack(BreadcrumbItem item, int position) {
                currentLocation = getPath(position);
                loadTask(null, currentLocation);
            }

            @Override
            public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {
                currentLocation = getPath(changedPosition - 1) + "/" + newItem.getSelectedItem();
                loadTask(null, currentLocation);
            }
        });

        String[] files = currentLocation.split("/");

        while (breadcrumbsView.getItems().size() != 0) {
            breadcrumbsView.removeLastItem();
        }

        breadcrumbsView.addItem(BreadcrumbItem.createSimpleItem("Root"));

        for (String file : files) {
            if (!file.equals("")) {
                breadcrumbsView.addItem(BreadcrumbItem.createSimpleItem(file));
            }
        }

        loadTask(null, currentLocation);
    }

    private String getCurrentPath() {
        return getPath(-1);
    }

    private String getPath(int depth) {
        if (breadcrumbsView == null) {
            return null;
        }
        try {
            if (depth == -1) depth = breadcrumbsView.getItems().size() - 1;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= depth; i++) {
                sb.append("/").append(breadcrumbsView.getItems().get(i).getSelectedItem());
            }
            if (sb.toString().equals("")) {
                sb.append("/");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onRestore() {
        currentLocation = getCurrentPath();
        loadTask(null, currentLocation);
    }

    public boolean onBackPressed() {
        if (breadcrumbsView == null) {
            return false;
        }
        currentLocation = getPath(breadcrumbsView.getItems().size() - 2);
        String path = getPath(breadcrumbsView.getItems().size() - 1);
        if (path != null && !path.equals(rootLocation)) {
            breadcrumbsView.removeLastItem();
            loadTask(null, currentLocation);
            return true;
        }
        return false;
    }

    private void setUpBottomController() {
        if (libraryBottomPlaybackController == null) {
            return;
        }
        boolean mainScreenStyle = SymphonyApplication.getInstance().getPreferenceUtils().getMainScreenStyle();
        if (!mainScreenStyle) {
            libraryBottomPlaybackController.setVisibility(View.GONE);
            return;
        }
        libraryBottomPlaybackController.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(getActivity()));
        libraryBottomPlaybackController.setProgressColor(ContrastColor(ThemeUtils.getThemeWindowBackgroundColor(getActivity())), getThemeAccentColor(getActivity()));
        libraryBottomPlaybackController.setOnClickEventDetectedListener(new LibraryBottomPlaybackController.OnClickEventDetectedListener() {
            @Override
            public void onPlayPreviousClicked() {
                playPrevious();
            }

            @Override
            public void onOpenPlayingQueueClicked() {
                openPlayingQueue();
            }

            @Override
            public void onPlayPauseClicked() {
                pauseOrResumePlayer();
            }

            @Override
            public void onPlayNextClicked() {
                playNext();
            }

            @Override
            public void onSongNameClicked() {
                openNowPlaying();
            }
        });
    }

    private void openNowPlaying() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getActivity(), TOAST_INFO);
        }
    }

    private void openPlayingQueue() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(getActivity(), QueueActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getActivity(), TOAST_INFO);
        }
    }
}