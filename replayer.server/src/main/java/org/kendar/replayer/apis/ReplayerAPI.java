package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.annotations.HttpTypeFilter;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPI implements FilteringClass {
}
