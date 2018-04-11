package org.ovgu.de.fiction.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author Suhita 
 */

/**
 * This is utility class for file operations
 */
public class FRFileOperationUtils {

	private static Logger LOG = Logger.getLogger(FRFileOperationUtils.class);

	/**
	 * @return
	 * @throws IOException
	 *             The method fetches file names present in the folder configured in the properties
	 *             file
	 */
	public static List<Path> getFileNames(String folderName) throws IOException {
		try (Stream<Path> paths = Files.walk(Paths.get(folderName))) {
			List<Path> filePathList = paths.filter(Files::isRegularFile).collect(Collectors.toList());
			return filePathList;
		}
	}

	/**
	 * @param src
	 * @param des
	 * @return The methods copies files from folder location passed in the method
	 */
	public static boolean copyFile(Path src, Path des) {
		File source = new File(src.toString());
		File dest = new File(des.toString());
		try {
			FileUtils.copyFileToDirectory(source, dest);
			return true;
		} catch (IOException e) {
			LOG.error("Files could not be copied -" + e.getMessage());
		}
		return false;
	}

	/**
	 * @param path
	 * @return
	 */
	public static String readFile(String path) {
		File file = new File(path);
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			return new String(bytes);
		} catch (IOException e) {
			LOG.error("Files could not be read -" + e.getMessage());
		}
		return "";
	}

}
