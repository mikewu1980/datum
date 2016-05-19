package org.mas.stock.usl.common;

import java.io.Serializable;

public class ResponseEntity implements Serializable {
	private static final long serialVersionUID = 5600732461682124950L;

	private int errorCode;

	private String errorMessage;

	private Object data;

	private transient String jsonpFunction;

	public ResponseEntity(Object data) {
		this.data = (data == null ? "" : data);
	}

	public ResponseEntity(ResultEnums resultEnum) {
		this.errorCode = resultEnum.getValue();
		this.errorMessage = resultEnum.getText();
		this.data = "";
	}

	public ResponseEntity(ResultEnums resultEnum, Object data) {
		this(resultEnum);
		this.data = (data == null ? "" : data);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getJsonpFunction() {
		return jsonpFunction;
	}

	public void setJsonpFunction(String jsonpFunction) {
		this.jsonpFunction = jsonpFunction;
	}
}
