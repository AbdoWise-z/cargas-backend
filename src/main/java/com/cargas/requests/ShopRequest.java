package com.cargas.requests;

import com.cargas.CargasBackendApplication;
import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ShopRequest {

    @GetMapping(value = "/shop")
    String getShop(@RequestParam Map<String , String> params){
        String token = params.get("token");
        if (token != null){
            return new Document(Map.of(
                    "result" , 0,
                    "shop" , CargasBackendApplication.getDatabase().getShop(token)
            )).toJson();
        }else{
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }
}
