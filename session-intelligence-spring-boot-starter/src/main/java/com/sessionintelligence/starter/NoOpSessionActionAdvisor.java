package com.sessionintelligence.starter;

import java.util.EnumSet;

import com.sessionintelligence.core.SessionAction;
import com.sessionintelligence.core.SessionActionAdvisor;
import com.sessionintelligence.core.SessionRiskScore;

public class NoOpSessionActionAdvisor implements SessionActionAdvisor {
    @Override
    public EnumSet<SessionAction> advise(SessionRiskScore score) {
        return EnumSet.of(SessionAction.LOG);
    }
}
