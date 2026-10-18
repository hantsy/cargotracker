package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class EventFilesCheckpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    // Path does not implement Serializable interface, using String fixes the failure on Wildfly
    // private List<Path> files = new LinkedList<>();
    private List<String> files = new LinkedList<>();
    private int fileIndex = 0;
    private long lineIndex = 0;

    public void setPaths(List<Path> files) {
        this.files = files.stream().map(Path::toString).toList();
    }

    public long getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(long lineIndex) {
        this.lineIndex = lineIndex;
    }

    public Path currentFile() {
        if (files.size() > fileIndex) {
            return Paths.get(files.get(fileIndex));
        } else {
            return null;
        }
    }

    public Path nextFile() {
        lineIndex = 0;

        if (files.size() > ++fileIndex) {
            return Paths.get(files.get(fileIndex));
        } else {
            return null;
        }
    }
}
