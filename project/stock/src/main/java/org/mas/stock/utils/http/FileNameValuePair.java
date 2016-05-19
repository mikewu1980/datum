package org.mas.stock.utils.http;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

public class FileNameValuePair {
	private final String name;
	private final File value;
	private final String fileName;
	private final ContentType contentType;
	private final String defaultMimeType = "application/octet-stream";

	public FileNameValuePair(String name, File value) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name is null or blank");
		}

		if (null == value) {
			throw new IllegalArgumentException("value is null");
		}

		if (value.exists() == false) {
			throw new IllegalArgumentException("value not exist");
		}

		if (value.isFile() == false) {
			throw new IllegalArgumentException("value is not file");
		}

		this.name = name;
		this.value = value;
		this.fileName = this.value.getName();

		String mimeType = null;
		try {
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			mimeType = fileNameMap.getContentTypeFor(value.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			mimeType = defaultMimeType;
		}
		this.contentType = ContentType.create(mimeType);
	}

	public FileNameValuePair(String name, File value, String fileName) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name is null or blank");
		}

		if (null == value) {
			throw new IllegalArgumentException("value is null");
		}

		if (value.exists() == false) {
			throw new IllegalArgumentException("value not exist");
		}

		if (value.isFile() == false) {
			throw new IllegalArgumentException("value is not file");
		}

		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("fileName is null or blank");
		}

		this.name = name;
		this.value = value;
		this.fileName = fileName;

		String mimeType = null;
		try {
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			mimeType = fileNameMap.getContentTypeFor(value.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			mimeType = defaultMimeType;
		}
		this.contentType = ContentType.create(mimeType);
	}

	public FileNameValuePair(String name, File value, String mimeType, String fileName) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name is null or blank");
		}

		if (null == value) {
			throw new IllegalArgumentException("value is null");
		}

		if (value.exists() == false) {
			throw new IllegalArgumentException("value not exist");
		}

		if (value.isFile() == false) {
			throw new IllegalArgumentException("value is not file");
		}

		if (StringUtils.isBlank(mimeType)) {
			throw new IllegalArgumentException("mimeType is null or blank");
		}

		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("fileName is null or blank");
		}

		this.name = name;
		this.value = value;
		this.contentType = ContentType.create(mimeType);
		this.fileName = fileName;
	}

	public String getName() {
		return name;
	}

	public File getValue() {
		return value;
	}

	public String getFileName() {
		return fileName;
	}

	public ContentType getContentType() {
		return contentType;
	}

}
