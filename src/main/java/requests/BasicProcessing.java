package requests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicProcessing {
    private Map<String, FileInformation> files;

    public BasicProcessing(List<String> files) {
        this.files = new HashMap<>();
        for (var file : files) {
            this.files.put(file, new FileInformation(file));
        }
    }

    public FileInformation getFileInformation(String file) {
        return files.get(file).update();
    }

    public void addFile(String file) {
        files.put(file, new FileInformation(file));
    }

    public void removeFile(String file) {
        files.remove(file);
    }
}
