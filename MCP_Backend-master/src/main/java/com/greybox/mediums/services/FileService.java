package com.greybox.mediums.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FileService {

    public Map<String, String> queryLatestAppVersion(Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        response.put("server_version", "1.0.1");
        return response;
    }
}
