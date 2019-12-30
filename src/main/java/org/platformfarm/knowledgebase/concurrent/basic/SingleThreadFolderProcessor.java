package org.platformfarm.knowledgebase.concurrent.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SingleThreadFolderProcessor {

    private final String path;
    private final String extension;

    public SingleThreadFolderProcessor(String path, String extension)
    {
        this.path = path;
        this.extension = extension;
    }

    public List<String> compute() {
        List<String> list = new ArrayList<String>();
        list.addAll(searchTargetFile(this.path, this.extension));
        return list;
    }

    private List<String> searchTargetFile(String path, String extension) {

        List<String> list = new ArrayList<String>();
        File file = new File(path);
        File content[] = file.listFiles();

        if (content != null) {
            for (File value : content) {
                if (value.isDirectory()) {
                    List<String> subList = searchTargetFile(value.getAbsolutePath(), extension);
                    list.addAll(subList);
                }
                else {
                    if (checkFile(value.getName())) {
                        list.add(value.getAbsolutePath());
                    }
                }
            }

        }
        return list;
    }


    private boolean checkFile(String name)
    {
        return name.endsWith(extension);
    }


}
