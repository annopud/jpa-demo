package dev.annopud.jpa_demo.controller;

import dev.annopud.jpa_demo.entity.GeneralTesting;
import dev.annopud.jpa_demo.service.GeneralTestingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Validated
@RestController
@RequestMapping("/general-testing")
public class GeneralTestingController {

    @Autowired
    private GeneralTestingService generalTestingService;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        generalTestingService.delete(id);
    }

    @GetMapping
    public List<GeneralTesting> findAll() {
        return generalTestingService.findAll();
    }

    @PostMapping
    public GeneralTesting create(@RequestBody GeneralTesting body) {

        // declare offset datetime fix date
        // instantiate OffsetDateTime by using ofInstant method
        OffsetDateTime fixedDate = OffsetDateTime
            .ofInstant(
                Instant.parse("2025-10-15T10:36:43.919940Z"),
                ZoneId.of("Asia/Bangkok")
            );

        OffsetDateTime dateTime = OffsetDateTime.of(
            2025, 10, 15, 12, 36, 43, 919940000,
            ZoneOffset.ofHours(7)
        );
//        body.setUpdateDate(OffsetDateTime.now(ZoneId.of("Europe/Paris")));

//        body.setCreateDate(Instant.parse("2025-10-15T10:36:43.919940Z"));
//        body.setUpdateDate(fixedDate);
//        2025-10-15T12:36:43.919940+02:00
        generalTestingService.save(body);

        return body;
    }

}
