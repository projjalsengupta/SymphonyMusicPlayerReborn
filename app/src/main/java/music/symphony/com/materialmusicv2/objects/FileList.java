package music.symphony.com.materialmusicv2.objects;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static music.symphony.com.materialmusicv2.utils.fileutils.FileUtils.isAudioFile;

public class FileList {

    public List<FileWrapper> directories;
    public List<FileWrapper> files;

    private FileList() {
    }

    public static FileList newInstance(String path) {
        FileList fileList = new FileList();

        Log.i("FileList", path);
        try {
            fileList.directories = Arrays.asList(FileWrapper.wraps(Objects.requireNonNull(new File(path).listFiles(file -> !file.isHidden() && file.isDirectory()))));
            Collections.sort(fileList.directories);
        } catch (Exception e) {
            fileList.directories = new ArrayList<>();
            e.printStackTrace();
        }

        try {
            fileList.files = Arrays.asList(FileWrapper.wraps(Objects.requireNonNull(new File(path).listFiles(file -> !file.isHidden() && file.isFile() && isAudioFile(file.getAbsolutePath())))));
            Collections.sort(fileList.files);
        } catch (Exception e) {
            fileList.files = new ArrayList<>();
            e.printStackTrace();
        }

        return fileList;
    }

    public static class FileWrapper implements Comparable<FileWrapper> {

        private File mFile;

        FileWrapper(File file) {
            mFile = file;
        }

        public boolean isDirectory() {
            return mFile.isDirectory();
        }

        public boolean isFile() {
            return mFile.isFile();
        }

        @Override
        public String toString() {
            return mFile.getName();
        }

        static FileWrapper[] wraps(File[] files) {
            FileWrapper[] array = new FileWrapper[files.length];
            for (int i = 0; i < files.length; i++) {
                array[i] = new FileWrapper(files[i]);
            }
            return array;
        }

        @Override
        public int compareTo(@NonNull FileWrapper fileWrapper) {
            if ((this.isDirectory() && fileWrapper.isDirectory()) || (this.isFile() && fileWrapper.isFile())) {
                return this.mFile.getName().toLowerCase().compareTo(fileWrapper.mFile.getName().toLowerCase());
            } else if (this.isFile() && fileWrapper.isDirectory()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}