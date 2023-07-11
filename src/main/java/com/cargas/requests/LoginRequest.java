package com.cargas.requests;

import com.arcore.Log;
import com.cargas.CargasBackendApplication;
import org.bson.Document;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginRequest {

    private static final Log log = Log.getLog(LoginRequest.class);

    @GetMapping(value =  {"/login"})
    public String doLogin(@RequestParam Map<String,String> params, ModelMap model){
        if (params.containsKey("user") && params.containsKey("password")) {
            String token = CargasBackendApplication.getDatabase().login(params.get("user") , params.get("password"));
            if (token != null){
                return new Document(Map.of(
                        "result" , 0,
                        "token" , token
                )).toJson();
            }

            return new Document(Map.of(
                    "result" , 1,
                    "error" , "Invalid login"
            )).toJson();

        }else{
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }
}
