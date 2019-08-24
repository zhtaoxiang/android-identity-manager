package com.ndn.jwtan.identitymanager;

import org.json.JSONException;
import org.json.JSONObject;

abstract class Challenge {
    abstract JSONObject genChallengeRequestJson(int status, final String challengeStatus, final JSONObject params) throws JSONException;

    abstract JSONObject getRequirementForChallenge(int applicationStatus, String challengeStatus) throws JSONException;

}
