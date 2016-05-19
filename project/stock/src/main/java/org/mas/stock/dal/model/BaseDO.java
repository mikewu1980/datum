package org.mas.stock.dal.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.BeanUtils;

public class BaseDO implements Serializable {
	/** UID */
	private static final long serialVersionUID = 2595958684348231467L;

	protected Long id;
	protected Date gmtCreate;
	protected Date gmtModify;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModify() {
		return gmtModify;
	}

	public void setGmtModify(Date gmtModify) {
		this.gmtModify = gmtModify;
	}

	public <T> T copyPropertiesTo(T target, String... ignoreProperties) {
		BeanUtils.copyProperties(this, target, ignoreProperties);
		return target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		int hashCode = 0;
		if (null != this.id) {
			hashCode = this.id.hashCode();
		}

		result = prime * result + hashCode;

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (null == this.getId()) {
			throw new UnsupportedOperationException("id is null");
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		BaseDO other = (BaseDO) obj;
		if (null == other.getId()) {
			throw new UnsupportedOperationException("id is null");
		}

		if (this.getId().equals(other.getId())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, new RecursiveToStringStyle()).toString();
	}

}
