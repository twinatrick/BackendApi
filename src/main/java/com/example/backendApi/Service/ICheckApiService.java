package com.example.backendApi.Service;

import java.io.IOException;

public interface ICheckApiService {
    String getApiOnlyUrl(String url) throws IOException;

    void getAquarkApiData() throws IOException;
}
