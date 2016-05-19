package org.mas.stock.utils;

public class StringUtils {
	/**
	 * 能出去更多的空白字符，比JDK提供的更强
	 * 
	 * @param str
	 * @return
	 */
	public static String trim(String str) {
		if (null == str) {
			return null;
		}
		if (str.length() == 0) {
			return str;
		}

		char[] chars = str.toCharArray();
		int length = str.length();
		int index = 0;

		// 从头往后判断空白字符
		while (Character.isWhitespace(chars[index])) {
			index++;
		}
		// 从后往前判断空白字符
		while (Character.isWhitespace(chars[length - 1])) {
			length--;
		}

		if ((index > 0) || (length < str.length())) {
			return str.substring(index, length);
		} else {
			return str;
		}
	}

}
