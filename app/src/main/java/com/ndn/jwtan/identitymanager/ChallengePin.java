package com.ndn.jwtan.identitymanager;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import static com.ndn.jwtan.identitymanager.Constants.JSON_CLIENT_SELECTED_CHALLENGE;
import static com.ndn.jwtan.identitymanager.Constants.STATUS_BEFORE_CHALLENGE;
import static com.ndn.jwtan.identitymanager.Constants.STATUS_CHALLENGE;

public class ChallengePin extends Challenge {

    private String CHALLENGE_TYPE = "";
    private final static String NEED_CODE = "need-code";
    private final static String WRONG_CODE = "wrong-code";
    private final static String JSON_PIN_CODE = "pin-code";

    ChallengePin(String CHALLENGE_TYPE) {
        this.CHALLENGE_TYPE = CHALLENGE_TYPE;
    }

    // For Client
    public JSONObject getRequirementForChallenge(int status, final String challengeStatus) throws JSONException {
        JSONObject result = new JSONObject();
        if (status == STATUS_BEFORE_CHALLENGE && challengeStatus.equals("")) {
            // do nothing
        } else if (status == STATUS_CHALLENGE && challengeStatus.equals(NEED_CODE)) {
            result.put(JSON_PIN_CODE, "Please_input_your_verification_code");
        } else if (status == STATUS_CHALLENGE && challengeStatus.equals(WRONG_CODE)) {
            result.put(JSON_PIN_CODE, "Incorrect_PIN_code_please_try_again");
        } else {
            Timber.i("Client's status and challenge status are wrong");
        }
        return result;
    }

    JSONObject genChallengeRequestJson(int status, final String challengeStatus, final JSONObject params) throws JSONException {
        JSONObject result = new JSONObject();

        if (status == STATUS_BEFORE_CHALLENGE && challengeStatus.equals("")) {
            // do nothing
            result.put(JSON_CLIENT_SELECTED_CHALLENGE, CHALLENGE_TYPE);
        } else if (status == STATUS_CHALLENGE && challengeStatus.equals(NEED_CODE)) {
            result.put(JSON_CLIENT_SELECTED_CHALLENGE, CHALLENGE_TYPE);
            result.put(JSON_PIN_CODE, params.getString(JSON_PIN_CODE));
        } else if (status == STATUS_CHALLENGE && challengeStatus.equals(WRONG_CODE)) {
            result.put(JSON_CLIENT_SELECTED_CHALLENGE, CHALLENGE_TYPE);
            result.put(JSON_PIN_CODE, params.getString(JSON_PIN_CODE));
        } else {
            Timber.i("Client's status and challenge status are wrong");
        }
        return result;
    }

}
