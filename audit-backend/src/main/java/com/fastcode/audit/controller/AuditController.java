package com.fastcode.audit.controller;

import com.fastcode.audit.application.AuditEventDto;
import com.fastcode.audit.application.AuditInput;
import com.fastcode.audit.application.AuditService;
import com.fastcode.audit.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private EntityManager entityManager;

    @GetMapping
    public List<AuditEventDto> getAllAuditLogs(@RequestParam(value = "search", required = false) String search,
                                               @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                               @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                               Sort sort) throws MalformedURLException {
        if (Sort.unsorted().equals(sort)) {
            sort = Sort.by("timestamp").descending();
        }

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);
        SearchCriteria searchCriteria = generateSearchCriteriaForAuditLogs(search);

        return auditService.findAll(searchCriteria, pageable);
    }

    // Search logic to handle search strings for LogAudit entity
    public static SearchCriteria generateSearchCriteriaForAuditLogs(String searchString){
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setType(3);

        List<SearchFields> searchFields = new ArrayList<SearchFields>();

        if(searchString != null && searchString.length() > 0) {
            String[] fields = searchString.split(";");

            for(String field: fields) {
                SearchFields searchField = new SearchFields();

                String fieldName = field.substring(0, field.indexOf('['));
                String operator = field.substring(field.indexOf('[') + 1, field.indexOf(']'));
                String searchValue = field.substring(field.indexOf('=') + 1);

                searchField.setFieldName(fieldName);
                searchField.setOperator(operator);

                if(!operator.equals("range")){
                    searchField.setSearchValue(searchValue);
                }
                else {
                    String[] rangeValues = searchValue.split(",");
                    if(!rangeValues[0].isEmpty()) {
                        String startingValue = rangeValues[0];
                        searchField.setStartingValue(startingValue);
                    }

                    if(rangeValues.length == 2) {
                        String endingValue = rangeValues[1];
                        searchField.setEndingValue(endingValue);
                    }
                }

                searchFields.add(searchField);
            }
        }

        searchCriteria.setFields(searchFields);
        return searchCriteria;
    }

    @PostMapping("/frontend")
    public ResponseEntity<String> frontendAudit(@RequestBody AuditInput input) {
        auditService.logAudit(input);
        return new ResponseEntity<>("Audit logged successfully", HttpStatus.OK);
    }


    @GetMapping("/action")
    public ResponseEntity<List<String>> getAllAction() {
        return new ResponseEntity<>(auditService.findAllAction(), HttpStatus.OK);
    }

    @GetMapping("/entities")
    public List<String> getEntityNames() {
        Metamodel metamodel = entityManager.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();
        List<String> entityNames = new ArrayList<>();
        for (EntityType<?> entity : entities) {
            entityNames.add(entity.getJavaType().getSimpleName());
        }
        return entityNames;
    }

}