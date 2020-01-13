package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * 디렉터리를 순회하면서 소속된 파일의 목록을 구하는 Task 정의
 *
 */
public class DirectoryTraversalTask extends RecursiveTask<List<String>> {

    private static final long serialVersionUID = 1L;
    private final String path;
    private final String extension;

    public DirectoryTraversalTask(String path, String extension) {
        this.path = path;
        this.extension = extension;
    }

    @Override
    protected List<String> compute() {
        List<String> list = new ArrayList<String>();
        List<DirectoryTraversalTask> tasks = new ArrayList<DirectoryTraversalTask>();
        File file = new File(path);
        File content[] = file.listFiles();
        if (content != null) {
            for (File value : content) {
                if (value.isDirectory()) { // 파일 객체가 디렉터리인 경우 작업을 분할 한다.
                    DirectoryTraversalTask task = new DirectoryTraversalTask(value.getAbsolutePath(), extension);
                    task.fork();
                    tasks.add(task);
                }
                else {
                    if (checkFile(value.getName())) {
                        list.add(value.getAbsolutePath());
                    }
                }
            }
        }

        addResultsFromTasks(list, tasks);

        return list;
    }

    private void addResultsFromTasks(List<String> list, List<DirectoryTraversalTask> tasks) {
        for (DirectoryTraversalTask item : tasks) {
            list.addAll(item.join());
        }
    }

    private boolean checkFile(String name) {
        return name.endsWith(extension);
    }
}
