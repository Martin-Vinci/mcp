package com.greybox.mediums.controllers;

import com.greybox.mediums.services.FileService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.FileDownloadUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@RestController
@RequestMapping("/api/v1/app")
@Api(tags = "AppService")
public class AppVersionEndPoint {

    @Autowired
    private FileService fileService;

    @PostMapping("/appVersion")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public ResponseEntity appVersion(Map<String, Object> request) {
        try {
            logInfo(request);
            return ResponseEntity.ok(fileService.queryLatestAppVersion(request));
        }catch (Exception e){
            logError(e);
            return ResponseEntity.ok(CommonResponse.getUndefinedError());
        }
    }

    @GetMapping("/appDownload")
    public ResponseEntity<?> appDownload() {
        FileDownloadUtility downloadUtil = new FileDownloadUtility();
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource("micropay_mobile.apk");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}
