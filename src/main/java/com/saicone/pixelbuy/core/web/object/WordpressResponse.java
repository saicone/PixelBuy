package com.saicone.pixelbuy.core.web.object;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.UnaryOperator;

public interface WordpressResponse {

    boolean isError();

    void throwError(@NotNull UnaryOperator<String> filter) throws IOException;
}
