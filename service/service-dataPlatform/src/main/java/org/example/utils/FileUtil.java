package org.example.utils;

public class FileUtil {
    /**
     * 判断是否为视频
     *
     * @param postfix
     * @return
     */
    public static boolean isVideoFile(String postfix) {
        String[] suffixes = {"mp4", "avi", "mov", "flv", "wmv"};
        for (String suffix : suffixes) {
            if (postfix.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为图片
     *
     * @param postfix
     * @return
     */
    public static boolean isImgFile(String postfix) {
        String[] suffixes = {"bmp", "jpg", "jpeg", "png", "gif"};
        for (String suffix : suffixes) {
            if (postfix.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileType(String postfix) {
        if (isImgFile(postfix)) {
            return "0";
        } else if (isVideoFile(postfix)) {
            return "1";
        } else if (postfix.equals("pdf")) {
            return "2";
        } else {
            return "3";
        }
    }

    /**
     * 判断文件大小
     *
     * @param len  文件长度
     * @param size 限制大小
     * @param unit 限制单位（B,K,M,G）
     * @return
     */
    public static boolean checkFileSize(Long len, int size, String unit) {
        double fileSize = 0;
        if ("B".equals(unit.toUpperCase())) {
            fileSize = (double) len;
        } else if ("K".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1024;
        } else if ("M".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1048576;
        } else if ("G".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1073741824;
        }
        if (fileSize > size) {
            return false;
        }
        return true;
    }
}
