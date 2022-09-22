package com.example.sppringbatch.config;

import java.util.List;

import com.example.sppringbatch.secondary.Manager;
import com.example.sppringbatch.secondary.ManagerRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyCustomWriter implements ItemWriter<Manager> {

    @Autowired
    private ManagerRepository managerRepository;

    @Override
    public void write(List<? extends Manager> list) throws Exception {
        System.out.println("Thread Name : -"+Thread.currentThread().getName());
        managerRepository.saveAll(list);
    }
}
