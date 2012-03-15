package org.nju.artemis.aejb.management.cli;

import java.io.File;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public abstract class FilenameTabCompleter {

	public String translatePath(String path) {
        final String translated;
        // special character: ~ maps to the user's home directory
        if (path.startsWith("~" + File.separator)) {
            translated = System.getProperty("user.home") + path.substring(1);
        } else if (path.startsWith("~")) {
            translated = new File(System.getProperty("user.home")).getParentFile().getAbsolutePath();
        } else if (!startsWithRoot(path)) {
            throw new IllegalArgumentException(ManagementMessages.FilePathNotCorrect + ": " + path);
        } else {
            translated = path;
        }
        return translated;
    }
	
	protected abstract boolean startsWithRoot(String path);
	
	public File getFile(String buffer) {

		if (buffer.length() >= 2 && buffer.charAt(0) == '"') {
			int lastQuote = buffer.lastIndexOf('"');
			if (lastQuote >= 0) {
				StringBuilder buf = new StringBuilder();
				buf.append(buffer.substring(1, lastQuote));
				if (lastQuote != buffer.length() - 1) {
					buf.append(buffer.substring(lastQuote + 1));
				}
				buffer = buf.toString();
			}
		}

		final String path;
		if (buffer.length() == 0) {
			path = null;
		} else {
			path = translatePath(buffer);
		}

		if (path == null)
			return null;
		File file = new File(path);
		if (file.exists())
			return file;
		else {
			return null;
		}
	}
}
