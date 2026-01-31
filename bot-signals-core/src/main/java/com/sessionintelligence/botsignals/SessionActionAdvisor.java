package com.sessionintelligence.botsignals;

import java.util.EnumSet;

public interface SessionActionAdvisor {
    EnumSet<SessionAction> advise(SessionRiskScore score);
}
