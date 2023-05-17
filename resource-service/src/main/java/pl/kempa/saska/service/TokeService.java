package pl.kempa.saska.service;

/**
 * It can be Redis or Spring Cache, but I just selected the simplest option
 */
public interface TokeService {
  void put(String traceId, String token);

  String get(String traceId);
}
