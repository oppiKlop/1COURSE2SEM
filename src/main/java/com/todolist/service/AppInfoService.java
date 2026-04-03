package com.todolist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppInfoService {

  private final String appName;
  private final String appVersion;

  public AppInfoService(
      @Value("${app.name}") String appName,
      @Value("${app.version}") String appVersion
  ) {
    this.appName = appName;
    this.appVersion = appVersion;
  }

  public String getAppName() {
    return appName;
  }

  public String getAppVersion() {
    return appVersion;
  }
}

