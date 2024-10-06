package com.xtest.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtest.dto.DynamicDto;
import com.xtest.exception.XTestException;
import com.xtest.service.XTestService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class XTestServiceImpl implements XTestService {

    private static final Logger logger = LoggerFactory.getLogger(XTestServiceImpl.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public byte[] process(MultipartFile textFile, MultipartFile excelFile, String apiConfiguration) {
        logger.info(" process method start ");
        List<Map<String, Object>> responseList = new LinkedList<>();
        byte[] resonseByte = null;
        try {
            Map apiConfig = mapper.readValue(apiConfiguration, Map.class);
            String requstSample = new String(textFile.getBytes());
            Map<String, Object> requstMap = mapper.readValue(requstSample, Map.class);
            List<Map<String, Object>> reqeustList = createReqeustList(requstMap, excelFile);
            callApi(apiConfig, reqeustList, responseList);
            resonseByte = writeExcel(responseList);
        } catch (IOException e) {
            throw new XTestException(e.getMessage(), 1000);
        }
        logger.info(" process method end ");
        return resonseByte;
    }

    private byte[] writeExcel(List<Map<String, Object>> responseList) {
        logger.info(" writeExcel method start ");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {

            Workbook workbook = new XSSFWorkbook(); // Create a new workbook
            Sheet sheet = workbook.createSheet("Test Cases"); // Create a
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Request", "Response", "Timing"}; // Example headers
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            AtomicInteger rowIndex = new AtomicInteger(1);
            responseList.forEach(res -> {
                Row row = sheet.createRow(rowIndex.getAndIncrement());
                row.createCell(0).setCellValue((String) res.get("requestBody"));
                Cell cell = row.createCell(1);
                cell.setCellValue((String) res.get("responseBody"));
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setWrapText(true);
                cell.setCellStyle(headerStyle);
                row.createCell(2).setCellValue((Double) res.get("timing"));
            });
            workbook.write(byteArrayOutputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info(" writeExcel method end ");
        return byteArrayOutputStream.toByteArray();
    }

    private void callApi(Map<String, Object> apiConfiguration, List<Map<String, Object>> reqeustList, List<Map<String, Object>> responseList) {
        logger.info(" callApi method start ");
        String url = (String) apiConfiguration.get("url");
        String apiKey = (String) apiConfiguration.get("apikay");
        if (apiConfiguration.isEmpty() || apiKey == null || url == null) {
            throw new XTestException("Invalid request", 1001);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("API-KEY", apiKey); // or use the appropriate header type
        headers.set("Content-Type", "application/json"); // Adjust as necessary
        int apihit = 1;
        for (Map requestData : reqeustList) {
            apihit++;
            try {
                logger.info(" api hit : {} ", apihit);
                HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestData), headers);
                long startTime = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                long duration = System.currentTimeMillis() - startTime;
                double durationSeconds = duration / 1000.0;
                responseList.add(new HashMap<>(Map.of("requestBody", mapper.writeValueAsString(requestData), "responseBody", response.getBody(), "timing", durationSeconds)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        logger.info(" callApi method end ");
    }

    private List<Map<String, Object>> createReqeustList(Map<String, Object> requstMap, MultipartFile excelFile) throws IOException {
        logger.info(" createReqeustList method start ");
        List<Map<String, Object>> requestList = new LinkedList<>();
        List<DynamicDto> dynamicDtos = processExcelFile(excelFile);
        dynamicDtos.forEach(dto -> {
            Map<String, Object> childRequest = new HashMap<>();
            requstMap.forEach((k, v) -> {
                if (dto.getProperties().containsKey(k) && dto.getProperty(k) != null) ;
                childRequest.put(k, dto.getProperty(k));
            });
            requestList.add(childRequest);
        });
        logger.info(" createReqeustList method end ");
        return requestList;
    }


    private List<DynamicDto> processExcelFile(MultipartFile excelFile) throws IOException {
        logger.info(" processExcelFile method start ");
        List<DynamicDto> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

            // Get headers from the first row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell headerCell : headerRow) {
                headers.add(headerCell.getStringCellValue());
            }

            // Iterate through the remaining rows
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);
                DynamicDto dynamicDto = new DynamicDto();

                for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                    Cell dataCell = dataRow.getCell(colIndex);
                    Object value = null;

                    if (dataCell != null) {
                        switch (dataCell.getCellType()) {
                            case STRING:
                                value = dataCell.getStringCellValue();
                                break;
                            case NUMERIC:
                                value = dataCell.getNumericCellValue();
                                break;
                            case BOOLEAN:
                                value = dataCell.getBooleanCellValue();
                                break;
                            case FORMULA:
                                value = dataCell.getCellFormula();
                                break;
                            default:
                                break;
                        }
                    }
                    dynamicDto.setProperty(headers.get(colIndex), value);
                }
                dataList.add(dynamicDto);
            }
        }
        logger.info(" processExcelFile method end ");
        return dataList;
    }

}
