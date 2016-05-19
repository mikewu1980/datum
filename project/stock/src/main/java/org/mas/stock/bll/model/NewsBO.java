package org.mas.stock.bll.model;

public class NewsBO extends BaseBO {
	/** UID */
	private static final long serialVersionUID = 3065708898267767597L;

	private String url;
	private String label;
	private String title;
	private String describe;
	private String content;
	private String publishTime;

	private boolean tradeData;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}

	public boolean isTradeData() {
		return tradeData;
	}

	public void setTradeData(boolean tradeData) {
		this.tradeData = tradeData;
	}

}
