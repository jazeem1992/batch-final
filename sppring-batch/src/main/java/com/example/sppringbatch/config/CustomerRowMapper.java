package com.example.sppringbatch.config;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.sppringbatch.primary.Employee;
import org.springframework.jdbc.core.RowMapper;

public class CustomerRowMapper implements RowMapper<Employee> {

    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Employee.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .salary(rs.getInt("salary"))
                .build();
    }
}
