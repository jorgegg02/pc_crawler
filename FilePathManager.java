import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;





public class FilePathManager {
    private ArrayList<String> filePaths;

    public FilePathManager() {
        filePaths = new ArrayList<String>();
    }

    public int getFileIDorAdd(String path) {
        int id = filePaths.indexOf(path);
        if (id == -1) {
            id = addFilePath(path);
        }
        return id;
    }
    public int addFilePath(String path) {
        filePaths.add(path);
        return filePaths.size() - 1;
    }

    public String getFilePath(int id) {
        return filePaths.get(id);
    }

    public String getFileName(int id){
        String path = getFilePath(id);
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
    public int getFileID(String path) {
        return filePaths.indexOf(path);
    }


}