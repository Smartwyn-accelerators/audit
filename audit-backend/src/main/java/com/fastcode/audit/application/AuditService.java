package com.fastcode.audit.application;

import com.fastcode.audit.domain.AuditEvent;
import com.fastcode.audit.domain.QAuditEvent;
import com.fastcode.audit.domain.IAuditRepository;
import com.fastcode.audit.search.SearchCriteria;
import com.fastcode.audit.search.SearchFields;
import com.querydsl.core.BooleanBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.audit4j.core.AuditManager;
import org.audit4j.core.dto.Field;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    @Qualifier("auditRepository")
    @NonNull protected final IAuditRepository _auditRepository;

    public List<AuditEventDto> findAll(SearchCriteria search, Pageable pageable) throws MalformedURLException {
        Page<AuditEvent> res = _auditRepository.findAll(search(search), pageable);
        return res.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<String> findAllAction() {
        return  _auditRepository.findAllAction();
    }

    public BooleanBuilder search(SearchCriteria search) throws MalformedURLException {

        QAuditEvent auditEvent = QAuditEvent.auditEvent;

        if (search != null) {
            Map<String, SearchFields> map = new HashMap<>();
            for (SearchFields fieldDetails : search.getFields()) {
                map.put(fieldDetails.getFieldName(), fieldDetails);
            }
            List<String> keysList = new ArrayList<String>(map.keySet());
            checkProperties(keysList);
            return searchKeyValuePair(auditEvent, map, search.getJoinColumns());
        }
        return null;
    }

    public void checkProperties(List<String> list) throws MalformedURLException {
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i).replace("%20", "").trim().equals("actor") ||
                    list.get(i).replace("%20", "").trim().equals("action") ||
                    list.get(i).replace("%20", "").trim().equals("origin") ||
                    list.get(i).replace("%20", "").trim().equals("path") ||
                    list.get(i).replace("%20", "").trim().equals("httpMethod") ||
                    list.get(i).replace("%20", "").trim().equals("eventTime") ||
                    list.get(i).replace("%20", "").trim().equals("entityName") ||
                    list.get(i).replace("%20", "").trim().equals("operation") ||
                    list.get(i).replace("%20", "").trim().equals("responseStatus") ||
                    list.get(i).replace("%20", "").trim().equals("exceptionType"))) {
                throw new MalformedURLException("Wrong URL Format: Property " + list.get(i) + " not found!");
            }
        }
    }

    public BooleanBuilder searchKeyValuePair(QAuditEvent auditEvent, Map<String, SearchFields> map, Map<String, String> joinColumns) {
        BooleanBuilder builder = new BooleanBuilder();

        for (Map.Entry<String, SearchFields> details : map.entrySet()) {

            if (details.getKey().replace("%20", "").trim().equals("actor")) {
                builder.and(auditEvent.actor.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));

            }
            if (details.getKey().replace("%20", "").trim().equals("action")) {
                builder.and(auditEvent.action.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));

            }
            if (details.getKey().replace("%20", "").trim().equals("origin")) {
                builder.and(auditEvent.origin.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("httpMethod")) {
                builder.and(auditEvent.elements.likeIgnoreCase("%httpMethod java.lang.String:" + details.getValue().getSearchValue() + "%"));
            }

            if (details.getKey().replace("%20", "").trim().equals("path")) {
                builder.and(auditEvent.elements.likeIgnoreCase("%path java.lang.String:" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("entityName")) {
                builder.and(auditEvent.elements.likeIgnoreCase("%entityName java.lang.String:" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("responseStatus")) {
                builder.and(auditEvent.elements.likeIgnoreCase("%responseStatus java.lang.String:" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("exceptionType")) {
                builder.and(auditEvent.elements.likeIgnoreCase("%exceptionType java.lang.String:" + details.getValue().getSearchValue() + "%"));
            }

            if (details.getKey().replace("%20", "").trim().equals("eventTime")) {
                String eventTimeStr = details.getValue().getSearchValue();
                LocalDateTime eventTime = getDateFromDateString(eventTimeStr);
                builder.and(auditEvent.timestamp.gt(Timestamp.valueOf(eventTime))); // Use 'gt' (greater than) for "after"
            }

        }
        return builder;
    }

    private LocalDateTime getDateFromDateString(String dateString) {

        String[] dateArray = dateString.split("-");
        dateString = dateArray[0] + "-" + (dateArray[1].length() == 1 ? "0" + dateArray[1] : dateArray[1]) + "-" + (dateArray[2].length() == 1 ? "0" + dateArray[2] : dateArray[2]);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate ld = LocalDate.parse(dateString, dateTimeFormatter);
        LocalDateTime ldt = LocalDateTime.of(ld, LocalTime.of(0,0));
        return ldt;

    }

    private AuditEventDto toDto(AuditEvent auditEvent) {
        if (auditEvent == null) {
            return null;
        }
        AuditEventDto dto = new AuditEventDto();
        dto.setIdentifier(auditEvent.getIdentifier());
        dto.setTimestamp(auditEvent.getTimestamp());
        dto.setActor(auditEvent.getActor());
        dto.setOrigin(auditEvent.getOrigin());
        dto.setAction(auditEvent.getAction());
        dto.setElements(parseFieldsFromString(auditEvent.getElements()));
        return dto;
    }

    public List<Field> parseFieldsFromString(String fieldString) {
        List<Field> fieldList = new ArrayList<>();

        if (fieldString != null && !fieldString.isEmpty()) {
            // Split by ", " to get each field representation
            String[] fieldPairs = fieldString.split(", ");
            for (String fieldPair : fieldPairs) {
                if (!fieldPair.isEmpty()) {
                    // Split the field representation by space and colon
                    String[] parts = fieldPair.split(" ", 2); // Split name and the rest (type:value)
                    if (parts.length == 2) {
                        String name = parts[0];

                        // Further split the type and value
                        String[] typeAndValue = parts[1].split(":", 2);
                        if (typeAndValue.length == 2) {
                            String type = typeAndValue[0];
                            String value = typeAndValue[1];

                            // Create Field object and add to list
                            fieldList.add(new Field(name, value, type));
                        }
                    }
                }
            }
        }

        return fieldList;
    }

    public org.audit4j.core.dto.AuditEvent logAudit(AuditInput input) {
        org.audit4j.core.dto.AuditEvent auditEvent = new org.audit4j.core.dto.AuditEvent();
        auditEvent.setAction(input.getAction());
        auditEvent.setActor(input.getUserId());
        auditEvent.addField("sessionId", input.getSessionId());
        input.getDetails().forEach(auditEvent::addField);
        AuditManager.getInstance().audit(auditEvent);
        return auditEvent;
    }
}
