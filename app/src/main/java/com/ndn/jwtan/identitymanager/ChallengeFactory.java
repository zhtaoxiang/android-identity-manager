package com.ndn.jwtan.identitymanager;

import java.util.HashMap;

public class ChallengeFactory {
    private static HashMap<String, Challenge> factory = new HashMap<>();

    public ChallengeFactory() {
        addChallenge();
    }

    private void addChallenge(){
        if (factory.size() == 0)
            factory.put("PIN", new ChallengePin("PIN"));
    }

     Challenge createChallengeModule(final String canonicalName) {
        addChallenge();
        if (factory.containsKey(canonicalName)) {
            return factory.get(canonicalName);
        } else return null;
    }

}
