package com.cargas.requests;

import com.cargas.CargasBackendApplication;
import com.cargas.core.Database;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegisterRequest {
    @PostMapping(value = "/register")
    public String doRegister(@RequestParam Map<String,String> params , @RequestBody String body){
        try {
            Document doc = Document.parse(body);

            String username     = doc.getString("username");
            String pass         = doc.getString("password");
            String name         = doc.getString("name");
            String phone_number = doc.getString("phone_number");
            String address      = doc.getString("address");

            if (username == null || pass == null || name == null || phone_number == null || address == null){
                return new Document(Map.of(
                        "result" , Database.REGISTER_INVALID_DATA,
                        "error" , "invalid data"
                )).toJson();
            }

            int res = CargasBackendApplication.getDatabase().register(
                    username,
                    pass,
                    name,
                    phone_number,
                    address
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
