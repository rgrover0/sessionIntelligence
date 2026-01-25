package com.sessionintelligence.core;

import java.util.EnumSet;

public interface SessionActionAdvisor {
    EnumSet<SessionAction> advise(SessionRiskScore score);
}
