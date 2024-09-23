package com.mawen.learn.mybatis.domain.misc;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.wrapper.BeanWrapper;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public class CustomBeanWrapper extends BeanWrapper {
	public CustomBeanWrapper(MetaObject metaObject, Object object) {
		super(metaObject, object);
	}
}
