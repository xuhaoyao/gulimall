package com.scnu.gulimall.order.config;

/*
import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class SeataConfig {


    @Autowired(required = true)
    private DataSourceProperties dataSourceProperties;

    */
/**
     * 去掉了自己配置的sqlSessionFactory,直接让DataSource bean返回的是一个被代理过的bean,
     * 并且加入了@Primary,导致mp优先使用我们配置的数据源,
     * 这样就解决了mp因为seata代理了数据源跟创建了新的sqlSessionFactory,导致mp的插件,组件失效的bug了
     * @return
     *//*

    @Primary
    @Bean
    public DataSource dataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dataSourceProperties.getUrl());
        druidDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        druidDataSource.setUsername(dataSourceProperties.getUsername());
        druidDataSource.setPassword(dataSourceProperties.getPassword());
        return  new DataSourceProxy(druidDataSource);
    }
}
*/
