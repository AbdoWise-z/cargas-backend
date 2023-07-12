package com.cargas.requests;


import com.cargas.CargasBackendApplication;
import com.cargas.core.Database;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LogoutRequest {

    @PostMapping(value = "/logout")
    String doLogout(@RequestBody String body){
        try {
            Document doc = Document.parse(body);

            String token     = doc.getString("token");

            if (token == null){
                return new Document(Map.of(
                        "result" , Database.REGISTER_INVALID_DATA,
                        "error" , "invalid data"
                )).toJson();
            }

            int res = CargasBackendApplication.getDatabase().logout(
                    token
            );

            return new Document(Map.of(
                    "result" , res
            )).toJson();

        }catch (Exception e){
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }
}
