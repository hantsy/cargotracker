package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class EventFilesCheckpoint implements Serializable {

	private List<Path> files = new LinkedList<>();

	private int fileIndex = 0;

	private long filePointer = 0;

	public void setFiles(List<Path> files) {
		this.files = files;
	}

	public long getFilePointer() {
		return filePointer;
	}

	public void setFilePointer(long filePointer) {
		this.filePointer = filePointer;
	}

	public Path currentFile() {
		if (files.size() > fileIndex) {
			return files.get(fileIndex);
		}
		else {
			return null;
		}
	}

	public Path nextFile() {
		filePointer = 0;

		if (files.size() > ++fileIndex) {
			return files.get(fileIndex);
		}
		else {
			return null;
		}
	}

}
