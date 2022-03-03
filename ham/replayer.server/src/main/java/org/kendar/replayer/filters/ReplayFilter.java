package org.kendar.replayer.filters;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*")
public class ReplayFilter implements FilteringClass {
  private final ReplayerStatus replayerStatus;
  private final String localAddress;

  public ReplayFilter(ReplayerStatus replayerStatus, JsonConfiguration configuration) {
    this.replayerStatus = replayerStatus;
    this.localAddress = configuration.getConfiguration(GlobalConfig.class).getLocalAddress();
  }

  @Override
  public String getId() {
    return "org.kendar.replayer.filters.RecordFilter";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.PRE_RENDER,
      pathAddress = "*",
      method = "*",
      id = "8000daa6-277f-11ec-9621-0242ac1afe002")
  public boolean replay(Request req, Response res) {
    if (req.getHost().equalsIgnoreCase(localAddress)) return false;
    if (replayerStatus.getStatus() != ReplayerState.REPLAYING && replayerStatus.getStatus() != ReplayerState.PLAYING_NULL_INFRASTRUCTURE) return false;
    return replayerStatus.replay(req, res);
  }
}
