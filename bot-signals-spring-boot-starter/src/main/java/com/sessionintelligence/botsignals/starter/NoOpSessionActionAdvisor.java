package com.sessionintelligence.botsignals.starter;

import java.util.EnumSet;

import com.sessionintelligence.botsignals.SessionAction;
import com.sessionintelligence.botsignals.SessionActionAdvisor;
import com.sessionintelligence.botsignals.SessionRiskScore;

public class NoOpSessionActionAdvisor implements SessionActionAdvisor {
    @Override
    public EnumSet<SessionAction> advise(SessionRiskScore score) {
        return EnumSet.of(SessionAction.LOG);
    }
}
