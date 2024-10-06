package com.xtest.controller;

import com.xtest.service.XTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("x_test")
public class XTestController {

    private static final Logger logger = LoggerFactory.getLogger(XTestController.class);
    @Autowired
	private XTestService service;

	@PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> uploadFiles(
            @RequestParam("textFile") MultipartFile textFile,
            @RequestParam("excelFile") MultipartFile excelFile, @RequestParam("apiConfig") String requestData) {
        logger.info(" uploadFiles api start ");
        byte[] response = service.process(textFile, excelFile, requestData);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Test_Cases.xlsx");
        logger.info(" uploadFiles api end ");
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

}
