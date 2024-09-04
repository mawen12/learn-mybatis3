package com.mawen.learn.mybatis.io;

import java.security.Security;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SerialFilterChecker {

	private static final Log log = LogFactory.getLog(SerialFilterChecker.class);

	private static final String JDK_SERIAL_FILTER = "jdk.serialFilter";
	private static final boolean SERIAL_FILTER_MISSING;
	private static boolean firstInvocation = true;

	static {
		Object serialFilter;
		try {
			Class<?> objectFilterConfig = Class.forName("java.io.ObjectInputFilter$Config");
			serialFilter = objectFilterConfig.getMethod("getSerialFilter").invoke(null);
		}
		catch (ReflectiveOperationException e) {
			serialFilter = System.getProperty(JDK_SERIAL_FILTER, Security.getProperty(JDK_SERIAL_FILTER));
		}
		SERIAL_FILTER_MISSING = serialFilter == null;
	}

	public static void check() {
		if (firstInvocation && SERIAL_FILTER_MISSING) {
			firstInvocation = false;
			log.warn("As you are using functionality that deserializes object streams, it is recommended to define the JEP-200 serial filter."
			 + "Please refer to https://docs.oracle.com/pls/topic/lookup?ctx=javase15&id=GUID-8296D8E8-2B93-4B9A-856E-0A65AF9B8C66");
		}
	}

	private SerialFilterChecker() {}
}
