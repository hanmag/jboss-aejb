package org.nju.artemis.aejb.management.cli;


/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class WindowsFilenameTabCompleter extends FilenameTabCompleter {

	@Override
	protected boolean startsWithRoot(String path) {
		return path.contains(":\\");
	}

}
