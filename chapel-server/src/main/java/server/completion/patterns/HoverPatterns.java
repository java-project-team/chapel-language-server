package server.completion.patterns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class HoverPatterns {
    private HashSet<String> patterns = new HashSet<>();
    private final HashSet<String> dataStructures = new HashSet<>();
    private final HashSet<String> languageSupport = new HashSet<>();

    public HoverPatterns() throws FileNotFoundException {
        dataStructures.add("Heap");
        dataStructures.add("List");
        dataStructures.add("Map");
        dataStructures.add("Set");

        languageSupport.add("Reflection");
        languageSupport.add("Types");
    }

    public HoverPatterns(String patternsFilePath) throws FileNotFoundException {
        File patternsDoc = new File(patternsFilePath);
        Scanner readFile = new Scanner(patternsDoc);
        while (readFile.hasNextLine()) {
            patterns.add(readFile.nextLine());
        }
    }

    public String isDataStructure(String value) {
        if (dataStructures.contains(value)) {
            return "https://chapel-lang.org/docs/modules/standard/" + value + ".html";
        } else {
            return "NONE";
        }
    }

    public String isLanguageSupport(String value) {
        if (languageSupport.contains(value)) {
            return "https://chapel-lang.org/docs/modules/standard/" + value + ".html";
        } else {
            return "NONE";
        }
    }

    public String hasDocumentation(String value) throws MalformedURLException, IOException {
        String adress = "https://chapel-lang.org/docs/modules/standard/" + value + ".html";
        URL url = new URL(adress);
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");
        huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        huc.connect();
        int responseCode = huc.getResponseCode();
        if (responseCode != 404) {
            return adress;
        } else {
            return "NONE";
        }
    }

}
