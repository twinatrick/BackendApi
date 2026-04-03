package com.example.backedapi.Service;

import java.io.IOException;

public interface IApiFetcher {
    String get(String url) throws IOException;
}
