package it.uniparthenope.fairwind.services.logger;


import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public interface DBHelper {
    public Integer insertFile(String path);
    public Integer insertFileInUploading(String path);
    public Integer deleteFile(String path);
    public Integer deleteFileFromUploading(String path);
    public ArrayList<String> getAllFiles();
    public boolean isUploading(String path);
    public ArrayList<String> getAllUploadingFiles();
    public int numberOfRows();
    public int numberOfUploadingRows();
    //public void updateFileStatus(String path);
    //public boolean isFileStatic(String path);
}