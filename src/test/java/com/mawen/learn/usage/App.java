package com.mawen.learn.usage;

import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.session.SqlSession;
import com.mawen.learn.mybatis.session.SqlSessionFactory;
import com.mawen.learn.mybatis.session.SqlSessionFactoryBuilder;
import com.mawen.learn.usage.entity.User;
import com.mawen.learn.usage.mapper.UserMapper;

import java.io.IOException;
import java.io.InputStream;

public class App {

    public static void main(String[] args) {
        String resource = "com/mawen/learn/usage/mybatis-config.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = sqlSessionFactory.openSession();

            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = mapper.selectById(1L);
            mapper.selectById(1L);

            mapper.selectById(2L);
            mapper.selectById(2L);

            User user3 = mapper.selectByIdNonCached(2L);
            mapper.selectByIdNonCached(2L);
            System.out.println(user3);

            session.commit();

            System.out.println(user.getUsername());

            session.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
