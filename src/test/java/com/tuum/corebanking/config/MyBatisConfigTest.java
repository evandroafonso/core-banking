package com.tuum.corebanking.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MyBatisConfigTest {

    private MyBatisConfig config;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        config = new MyBatisConfig();
        dataSource = mock(DataSource.class);
    }

    @Test
    void sqlSessionFactoryShouldBuildFactoryWithGivenDataSource() throws Exception {
        SqlSessionFactory sqlSessionFactory = config.sqlSessionFactory(dataSource);

        assertThat(sqlSessionFactory).isNotNull();
        assertThat(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource()).isEqualTo(dataSource);
    }

    @Test
    void sqlSessionTemplateShouldWrapGivenSqlSessionFactory() throws Exception {
        SqlSessionFactory sqlSessionFactory = config.sqlSessionFactory(dataSource);

        SqlSessionTemplate sqlSessionTemplate = config.sqlSessionTemplate(sqlSessionFactory);

        assertThat(sqlSessionTemplate).isNotNull();
        assertThat(sqlSessionTemplate.getSqlSessionFactory()).isEqualTo(sqlSessionFactory);
    }

    @Test
    void transactionManagerShouldBeDataSourceTransactionManagerWithGivenDataSource() {
        PlatformTransactionManager transactionManager = config.transactionManager(dataSource);

        assertThat(transactionManager).isInstanceOf(DataSourceTransactionManager.class);
        assertThat(((DataSourceTransactionManager) transactionManager).getDataSource()).isEqualTo(dataSource);
    }
}