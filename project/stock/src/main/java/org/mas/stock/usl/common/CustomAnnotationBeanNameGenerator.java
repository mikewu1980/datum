package org.mas.stock.usl.common;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * <p>
 * 修改Spring的注解扫描时bean的id的默认生成方式。
 * </p>
 * <label>即：带上包路径，防止冲突。</label> <label>比如：将类com.xxxx.xxx.ClassName => className；</label>
 * <label>变为:com.xxxx.xxx.ClassName => com.xxxx.xxx.ClassName</label>
 * <label>注：http://yunzhongxia.iteye.com/blog/898433</label>
 */
public class CustomAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {
	@Override
	protected String buildDefaultBeanName(BeanDefinition definition) {
		return definition.getBeanClassName();
	}

}