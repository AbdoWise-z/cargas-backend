package com.cargas.requests;


import com.cargas.CargasBackendApplication;
import com.cargas.core.Database;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoRequest {

    @PostMapping(value = {"/profile" , })
    String getInfo(@RequestBody String body){
        try {
            Document doc = Document.parse(body);

            String token     = doc.getString("token");

            if (token == null){
                return new Document(Map.of(
                        "result" , Database.REGISTER_INVALID_DATA,
                        "error" , "invalid data"
                )).toJson();
            }

            String res = CargasBackendApplication.getDatabase().getInfo(
                    token
            );

            if (res != null) {
                return new Document(Map.of(
                        "result", 0,
                        "info" , res
                        )).toJson();
            }else{
                return new Document(Map.of(
                        "result", 1,
                        "error", "invalid token"
                )).toJson();
            }

        }catch (Exception e){
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }
}
