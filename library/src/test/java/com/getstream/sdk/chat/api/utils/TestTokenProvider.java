package com.getstream.sdk.chat.api.utils;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.interfaces.TokenProvider;

/*
 * Created by Anton Bevza on 2019-10-18.
 */
public class TestTokenProvider implements CachedTokenProvider {
    public static String TEST_TOKEN = "testToken";

    @Override
    public void getToken(TokenProvider.TokenProviderListener listener) {
        listener.onSuccess(TEST_TOKEN);
    }

    @Override
    public void tokenExpired() {

    }
}
