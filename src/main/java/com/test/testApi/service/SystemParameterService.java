package com.test.testApi.service;

import com.test.testApi.repository.SystemParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;

    public int getInt(String key, int defaultValue) {
        return systemParameterRepository.findByParamKey(key)
                .map(p -> {
                    try {
                        return Integer.parseInt(p.getParamValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
