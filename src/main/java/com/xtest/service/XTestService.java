package com.xtest.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface XTestService {

	byte[] process(MultipartFile textFile, MultipartFile excelFile, String requestData);

}
