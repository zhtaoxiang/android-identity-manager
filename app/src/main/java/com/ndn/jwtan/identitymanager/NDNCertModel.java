package com.ndn.jwtan.identitymanager;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.named_data.jndn.security.v2.CertificateV2;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NDNCertModel extends ViewModel {
    Client client;

    public ArrayList<ClientCaItem> caItems;
    public MutableLiveData<List<String>> probes = new MutableLiveData<>();
    public JSONObject probesInfo = new JSONObject();
    public MutableLiveData<ClientCaItem> selectedCaItem = new MutableLiveData<>();
    public boolean nameEditable = false;
    public MutableLiveData<List<String>> challenges = new MutableLiveData<>();
    public MutableLiveData<JSONObject> requirement = new MutableLiveData<>();
    public JSONObject inputRequirement = new JSONObject();
    public MutableLiveData<CertificateV2> cert = new MutableLiveData<>();

}
