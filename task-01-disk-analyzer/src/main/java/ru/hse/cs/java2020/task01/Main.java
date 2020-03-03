package ru.hse.cs.java2020.task01;


//Directories and their sizes are not taken into account
import java.io.File;
//import java.io.FilenameFilter;
import java.util.*;

public class Main {
    private static final int TOPSIZE = 5;

    static class FileComparator implements Comparator<File> {
        public int compare(File a, File b) {
            return Long.compare(a.length(), b.length());

        }
    }

    static class PathComparator implements Comparator<File> {
        public int compare(File a, File b) {
            return Long.compare(a.getPath().length(), b.getPath().length());
        }
    }

    static class InfoComparator implements Comparator<FolderInfo> {
        public int compare(FolderInfo a, FolderInfo b) {
            return Long.compare(a.size, b.size);
        }
    }

    static class FolderInfo {
        private File path;
        private long items, size;

        FolderInfo(File toFolder) {
            path = toFolder;
            items = 0;
            size = 0;
        }
    }

    static class Result {
        private long size;
        private Queue<File> topFiles;
        private List<FolderInfo> dirInfo;

        Result() {
            size = 0;
            topFiles = new PriorityQueue<>(TOPSIZE, new FileComparator());
            dirInfo = new ArrayList<>();
        }

    }


    private static Result findDirInfo(File directory) {
        ArrayDeque<File> fileQueue = new ArrayDeque<>();
        int i = 0;
        Result res = new Result();

        File[] dirs = directory.listFiles((current, name) -> new File(current, name).isDirectory());

        for (File dir : Objects.requireNonNull(dirs)) {
            res.dirInfo.add(new FolderInfo(dir));
            Collections.addAll(fileQueue, Objects.requireNonNull(dir.listFiles()));
            while (!fileQueue.isEmpty()) {
                File file = fileQueue.remove();
                if (file.isDirectory()) {
                    Collections.addAll(fileQueue, Objects.requireNonNull(file.listFiles()));
                } else {
                    if (res.topFiles.size() < TOPSIZE) {
                        res.topFiles.add(file);
                    } else if (Objects.requireNonNull(res.topFiles.peek()).length() < file.length()) {
                        res.topFiles.poll();
                        res.topFiles.add(file);
                    }
                    res.size += file.length();
                    res.dirInfo.get(i).size += file.length();
                    res.dirInfo.get(i).items++;
                }
            }
            ++i;
        }

        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file:files) {
            if (!file.isDirectory()) {
                res.size += file.length();
                if (res.topFiles.size() < TOPSIZE) {
                    res.topFiles.add(file);
                } else if (Objects.requireNonNull(res.topFiles.peek()).length() < file.length()) {
                    res.topFiles.poll();
                    res.topFiles.add(file);
                }
                res.dirInfo.add(new FolderInfo(file));
                res.dirInfo.get(res.dirInfo.size() - 1).size = file.length();
            }
        }

        return res;
    }

    private static void printAll(String arg, Result res, long startTime) {
        final int measureTime = 1000;
        final int measureSize = 1024;
        final int toPercentage = 100;
        final int strBias = 3;

        List<Integer> topDirs = new ArrayList<>();
        for (int i = 0; i < res.dirInfo.size(); i++) {
            FolderInfo info = res.dirInfo.get(i);
            topDirs.add(info.path.getPath().substring(arg.length()).length());
        }
        res.dirInfo.sort(new InfoComparator().reversed());
        int maxPath = Collections.max(topDirs);
        System.out.printf("%-10s | %-" + maxPath + "s | %-18s | %-12s | %-15s \n",
                "Number", "Path", "Total size", "Percentage", "Number of files");
        for (int i = 0; i < res.dirInfo.size(); i++) {
            FolderInfo info = res.dirInfo.get(i);

            System.out.printf("%-10s | %-" + maxPath + "s | %-15d Kb | %-11f %%| %-15d items\n", (i + 1) + ".",
                    info.path.getPath().substring(arg.length()),
                    info.size / measureSize, (double) info.size * toPercentage / res.size, info.items);
        }


        List<File> topFiles = new ArrayList<>();
        for (int i = 0; i < TOPSIZE; i++) {
            topFiles.add(res.topFiles.remove());
        }
        long maxPathLength = Collections.max(topFiles, new PathComparator()).getPath().length();
        long maxSize = Integer.toString((int) (Collections.max(topFiles,
                new FileComparator()).length() / measureSize)).length();
        System.out.println("\n\n_ _ _ _ _ _ _ _ _ _BIGGEST FILES_ _ _ _ _ _ _ _ _ _\n");
        System.out.printf("%-6s | " + "%-" + maxPathLength + "s | %-" + (maxSize + strBias) + "s \n",
                "Number", "Path", "Size");
        for (int i = 1; i <= TOPSIZE; i++) {
            int ind = TOPSIZE - i;
            System.out.printf("%-6s | " + "%-" + maxPathLength + "s | %-" + maxSize + "d Kb \n", i
                    + ".", topFiles.get(ind).getPath(), topFiles.get(ind).length() / measureSize);
        }

        System.out.println("\nTotal size: " + res.size / measureSize + " Kb\nTotal time: "
                + ((System.currentTimeMillis() - startTime) / measureTime)  + "."
                + ((System.currentTimeMillis() - startTime) % measureTime) + " s");

    }

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();

        File dirPath;
        if (args.length != 1) {
            System.err.println("Wrong number of arguments!");
            return;
        } else {
            dirPath = new File(args[0]);
        }

        if (!dirPath.isDirectory() || !dirPath.exists()) {
            System.err.println("Wrong path!");
            return;
        }

        Result res = findDirInfo(dirPath);
        printAll(args[0], res, startTime);
    }
}
