package com.tenorinho.poc_batch_redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BuildInfoActuatorContributor implements InfoContributor {
  @Value("${build.group}")
  private String buildGroup;
  @Value("${build.artifact}")
  private String buildArtifact;
  @Value("${build.version}")
  private String buildVersion;
  @Value("${build.name}")
  private String buildName;
  @Value("${build.time}")
  private String buildTime;

  public BuildInfoActuatorContributor(){}

  @Override
  public void contribute(Info.Builder builder) {
    Map<String, String> buildInfoMap = new HashMap<String,String>();
    buildInfoMap.put("group", buildGroup);
    buildInfoMap.put("artifact", buildArtifact);
    buildInfoMap.put("version", buildVersion);
    buildInfoMap.put("name", buildName);
    buildInfoMap.put("time", buildTime);
    builder.withDetail("buildInfo", buildInfoMap);
  }
}
