package com.example.BackendApi.Service;

import java.io.IOException;

public interface IApiFetcher {
    String get(String url) throws IOException;
}
