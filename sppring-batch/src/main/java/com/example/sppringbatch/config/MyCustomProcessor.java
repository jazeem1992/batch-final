package com.example.sppringbatch.config;

import com.example.sppringbatch.primary.Employee;
import com.example.sppringbatch.secondary.Manager;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MyCustomProcessor implements ItemProcessor<Employee, Manager> {

    @Override
    public Manager process(Employee emp) throws Exception {
        System.out.println("MyBatchProcessor : Processing data : "+emp);
        Manager manager = new Manager();
        //manager.setId(emp.getId());
        manager.setName(emp.getName().toUpperCase());
        manager.setSalary(emp.getSalary());
        return manager;
    }
}

