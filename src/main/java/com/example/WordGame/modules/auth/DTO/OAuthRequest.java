package com.example.WordGame.modules.auth.DTO;

import lombok.Data;

@Data
public class OAuthRequest {
    // provider as a string (email, google, microsoft, apple)
    private String provider;
    // id_token or access token from the client
    private String token;
}
