package pl.kempa.saska.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import pl.kempa.saska.service.TokeService;

@Service
public class TokeServiceImpl implements TokeService {
  private Map<String, String> tokenMap = new HashMap<>();

  @Override
  public void put(String traceId, String token) {
    tokenMap.put(traceId, token);
  }

  @Override
  public String get(String traceId) {
    return tokenMap.get(traceId);
  }
}
