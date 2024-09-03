package com.mawen.learn.mybatis.reflection.property;

import java.lang.reflect.Field;

import com.mawen.learn.mybatis.reflection.Reflector;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public final class PropertyCopier {

	public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
		Class<?> parent = type;
		while (parent != null) {
			Field[] fields = parent.getDeclaredFields();

			for (Field field : fields) {
				try {
					try {
						field.set(destinationBean, field.get(sourceBean));
					}
					catch (IllegalAccessException e) {
						if (Reflector.canControlMemberAccessible()) {
							field.setAccessible(true);
							field.set(destinationBean, field.get(sourceBean));
						}
						else {
							throw e;
						}
					}
				}
				catch (Exception e) {
					// Nothing useful to do, will only fail on final fields, which will be ignored.
				}
			}

			parent = parent.getSuperclass();
		}
	}

	private PropertyCopier() {}
}
