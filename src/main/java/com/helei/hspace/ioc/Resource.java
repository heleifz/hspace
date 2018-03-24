package com.helei.hspace.ioc;

/**
 * abstract resource
 */
public interface Resource<T>
{
    public T resolve() throws Exception;

    default public T forceResolve() throws Exception {
        return resolve();
    }
    /**
     * add dependency (id) to resource
     */
    default public Resource<T> depends(String property, String id) {
        throw new UnsupportedOperationException();
    }

    /**
     * add dependency value to resource
     */
    default public Resource<T> dependsValue(String property, Object value) {
        throw new UnsupportedOperationException();
    }
}