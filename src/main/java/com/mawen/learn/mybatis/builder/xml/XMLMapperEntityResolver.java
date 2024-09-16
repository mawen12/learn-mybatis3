package com.mawen.learn.mybatis.builder.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.mawen.learn.mybatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class XMLMapperEntityResolver implements EntityResolver {

	private static final String IBATIS_CONFIG_SYSTEM = "ibatis-3-config.dtd";
	private static final String IBATIS_MAPPER_SYSTEM = "ibatis-3-mapper.dtd";
	private static final String MYBATIS_CONFIG_SYSTEM = "mybatis-3-config.dtd";
	private static final String MYBATIS_MAPPER_SYSTEM = "mybatis-3-mapper.dtd";

	private static final String MYBATIS_CONFIG_DTD = "com/mawen/learn/mybatis/builder/xml/mybatis-3-config.dtd";
	private static final String MYBATIS_MAPPER_DTD = "com/mawen/learn/mybatis/builder/xml/mybatis-3-mapper.dtd";


	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		try {
			if (systemId != null) {
				String lowerCaseSystemId = systemId.toLowerCase(Locale.ENGLISH);
				if (lowerCaseSystemId.contains(MYBATIS_CONFIG_SYSTEM) || lowerCaseSystemId.contains(IBATIS_CONFIG_SYSTEM)) {
					return getInputSource(MYBATIS_CONFIG_DTD, publicId, systemId);
				}
				else if (lowerCaseSystemId.contains(MYBATIS_MAPPER_SYSTEM) || lowerCaseSystemId.contains(IBATIS_MAPPER_SYSTEM)) {
					return getInputSource(MYBATIS_MAPPER_DTD, publicId, systemId);
				}
			}
			return null;
		}
		catch (Exception e) {
			throw new SAXException(e.toString());
		}
	}

	private InputSource getInputSource(String path, String publicId, String systemId) {
		InputSource source = null;
		if (path != null) {
			try {
				InputStream in = Resources.getResourceAsStream(path);
				source = new InputSource(in);
				source.setPublicId(publicId);
				source.setSystemId(systemId);
			}
			catch (IOException ignored) {

			}
		}
		return source;
	}
}
