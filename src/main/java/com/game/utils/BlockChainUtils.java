package com.game.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author yanfang
 * &#064;date  2024/6/4 15:57
 * @version 1.0
 */

@Slf4j
@Service
public class BlockChainUtils {
    static final String CONTRACT_ABI = FileUtil.readUtf8String("contract.abi");
    @Value("${contract.adminAddress}")
    public String adminAddress;
    @Value("${contract.URL}")
    String URL;
    @Value("${contract.name}")
    String CONTRACT_NAME;
    @Value("${contract.Address}")
    String CONTRACT_ADDRESS;

    public JSONObject returnJSON(String userAddress, String methodName, JSONArray methodParams) {
        RestTemplate restTemplate = new RestTemplate();//http对象
        //jsonObj对象，储存参数
        JSONObject jsonObj = base(userAddress, methodName, methodParams);
        //System.out.println("base:"+jsonObj);

        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(URL, jsonObj, JSONObject.class);
        return responseEntity.getBody();
    }

    public String returnString(String userAddress, String methodName, JSONArray methodParams) {
        RestTemplate restTemplate = new RestTemplate();//http对象
        //jsonObj对象，储存参数
        JSONObject jsonObj = base(userAddress, methodName, methodParams);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(URL, jsonObj, String.class);
        return responseEntity.getBody();
    }

    //集成的参数
    private JSONObject base(String userAddress, String methodName, JSONArray methodParams) {
        //jsonObj对象，储存参数
        JSONObject jsonObj = new JSONObject();
        //System.out.println("用户地址"+ CONTRACT_ADDRESS);

        jsonObj.putOpt("contractName", CONTRACT_NAME);
        jsonObj.putOpt("contractAddress", CONTRACT_ADDRESS);
        jsonObj.putOpt("contractAbi", JSONUtil.parseArray(CONTRACT_ABI));
        jsonObj.putOpt("user", userAddress);
        jsonObj.putOpt("funcName", methodName);
        jsonObj.putOpt("funcParam", methodParams);
        return jsonObj;
    }
}
