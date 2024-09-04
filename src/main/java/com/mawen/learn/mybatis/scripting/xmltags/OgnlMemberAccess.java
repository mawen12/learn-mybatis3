package com.mawen.learn.mybatis.scripting.xmltags;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Map;

import com.mawen.learn.mybatis.reflection.Reflector;
import ognl.MemberAccess;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class OgnlMemberAccess implements MemberAccess {

	private final boolean canControlMemberAccessible;

	OgnlMemberAccess() {
		this.canControlMemberAccessible = Reflector.canControlMemberAccessible();
	}

	@Override
	public Object setup(Map context, Object target, Member member, String propertyName) {
		Object result = null;
		if (isAccessible(context, target, member, propertyName)) {
			AccessibleObject accessible = (AccessibleObject) member;
			if (!accessible.isAccessible()) {
				result = Boolean.FALSE;
				accessible.setAccessible(true);
			}
		}
		return result;
	}

	@Override
	public void restore(Map map, Object o, Member member, String s, Object o1) {
		// nothing
	}

	@Override
	public boolean isAccessible(Map map, Object o, Member member, String s) {
		return canControlMemberAccessible;
	}
}
