package com.example.sppringbatch.config;

import com.example.sppringbatch.primary.Employee;
import com.example.sppringbatch.secondary.Manager;
import com.example.sppringbatch.secondary.ManagerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.batch.item.database.Order;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("secondaryDataSource")
    private DataSource dataSourceSecondary;

    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource dataSourcePrimary;

    @Autowired
    ManagerRepository managerRepository;

    @Bean
    public ColumnRangePartitioner partitioner() {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn("id");
        columnRangePartitioner.setDataSource(dataSourcePrimary);
        columnRangePartitioner.setTable("employee");
        return columnRangePartitioner;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Employee> pagingItemReader(@Value("#{stepExecutionContext['minValue']}") Long minValue, @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        System.out.println("reading " + minValue + " to " + maxValue);

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, name, salary");
        queryProvider.setFromClause("from employee");
        queryProvider.setWhereClause("where id >= " + minValue + " and id < " + maxValue);
        queryProvider.setSortKeys(sortKeys);

        JdbcPagingItemReader<Employee> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSourcePrimary);
        reader.setFetchSize(1000);
        reader.setRowMapper(new CustomerRowMapper());
        reader.setQueryProvider(queryProvider);
        return reader;
    }

    // Master
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    // slave step
    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slaveStep")
                .<Employee, Manager>chunk(250)
                .reader(pagingItemReader(null, null))
                .writer(customerItemWriter())
                .build();
    }


    @Bean
    @StepScope
    public JdbcBatchItemWriter<Manager> customerItemWriter() {
        JdbcBatchItemWriter<Manager> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(dataSourceSecondary);
        itemWriter.setSql("INSERT INTO manager VALUES (:id, :name, :salary)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job").start(step1()).build();
    }

    @Autowired
    private MyCustomReader myCustomReader;

    @Autowired
    private MyCustomWriter myCustomWriter;

    @Autowired
    private MyCustomProcessor myCustomProcessor;
}