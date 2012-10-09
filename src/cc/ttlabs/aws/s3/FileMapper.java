package cc.ttlabs.aws.s3;

import java.io.File;

public interface FileMapper {

	boolean isRootFolder(String rootFolderName);

	void createRootFolder(String rootFolderName);

	void deleteRootFolder(String rootFolderName);

	void uploadFile(String rootFolderName, String fileName, File file);

	void deleteFile(String rootFolderName, String fileName);

	void makePublic(String rootFolderName, String string);

	boolean isFilePublic(String rootFolderName, String keyName);

	boolean downloadFile(String rootFolderName, String keyNameNotAvailable,
			File outputFile);

}
