package com.helei.hspace.ioc;

import java.util.function.Supplier;

public class LambdaResource<T> implements Resource<T>
{
    private Supplier<T> supplier;

    public LambdaResource(Supplier<T> supplier) {
        this.supplier = supplier;
    } 

    @Override
    public T resolve() {
        return supplier.get();
    }
}