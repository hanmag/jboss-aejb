package org.nju.artemis.aejb.management.cli;

import java.io.File;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class DefaultFilenameTabCompleter extends FilenameTabCompleter {

	@Override
	protected boolean startsWithRoot(String path) {
		return path.startsWith(File.separator);
	}

}
