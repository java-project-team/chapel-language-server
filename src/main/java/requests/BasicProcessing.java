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
        if (!files.containsKey(file)) {
            System.out.println("file: " + file);
            files.put(file, new FileInformation(file));
        }
        else {
            files.get(file).update();
        }
    }

    public void removeFile(String file) {
        files.remove(file);
    }
}
