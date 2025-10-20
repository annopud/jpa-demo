package dev.annopud.jpa_demo.service;

import dev.annopud.jpa_demo.entity.GeneralTesting;
import dev.annopud.jpa_demo.repository.GeneralTestingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class GeneralTestingService {

    @Autowired
    private GeneralTestingRepository generalTestingRepository;

    public void delete(String id) {
        generalTestingRepository.deleteById(id);
    }

    private GeneralTesting requireOne(String id) {
        return generalTestingRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
    }

    public java.util.List<GeneralTesting> findAll() {
        return generalTestingRepository.findAll();
    }

    public void save(GeneralTesting body) {
        generalTestingRepository.save(body);
    }
}
