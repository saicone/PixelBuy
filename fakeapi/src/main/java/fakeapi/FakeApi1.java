package fakeapi;

import org.jetbrains.annotations.Nullable;

public class FakeApi1 {

    @Nullable
    public static final FakeItem1 itemFromId(@Nullable String id) {
        throw new RuntimeException("empty method");
    }
}
