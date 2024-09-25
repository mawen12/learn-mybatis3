package com.mawen.learn.mybatis.type;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.BaseDataTest;
import com.mawen.learn.mybatis.annotations.Insert;
import com.mawen.learn.mybatis.annotations.Select;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.SqlSession;
import com.mawen.learn.mybatis.session.SqlSessionFactory;
import com.mawen.learn.mybatis.session.SqlSessionFactoryBuilder;
import com.mawen.learn.mybatis.transaction.TransactionFactory;
import com.mawen.learn.mybatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public class BlobInputStreamTypeHandlerIntegrationTest {

	private static SqlSessionFactory sqlSessionFactory;

	@BeforeAll
	static void setUp() throws IOException, SQLException {
		DataSource dataSource = BaseDataTest.createUnpooledDataSource("com/mawen/learn/mybatis/type/jdbc.properties");
		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("Production", transactionFactory, dataSource);
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(Mapper.class);
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

		BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
				"com/mawen/learn/mybatis/type/BlobInputStreamTypeHandlerTest.sql");
	}

	@Test
	void integrationTest() throws IOException {
		try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
			Mapper mapper = sqlSession.getMapper(Mapper.class);

			{
				BlobContent blobContent = new BlobContent();
				blobContent.setId(1);
				blobContent.setContent(new ByteArrayInputStream("Hello".getBytes()));

				mapper.insert(blobContent);
				sqlSession.commit();
			}

			{
				BlobContent blobContent = mapper.findOne(1);
				assertEquals("Hello", new BufferedReader(new InputStreamReader(blobContent.getContent())).readLine());
			}
		}
	}

	interface Mapper {
		@Select("SELECT ID CONTENT FROM TEST_BLOB WHERE ID = #{id}")
		BlobContent findOne(int id);

		@Insert("INSERT INTO TEST_BLOB (ID, CONTENT) VALUES(#{id}, #{content})")
		void insert(BlobContent blobContent);
	}

	static class BlobContent {
		private int id;
		private InputStream content;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public InputStream getContent() {
			return content;
		}

		public void setContent(InputStream content) {
			this.content = content;
		}
	}
}
