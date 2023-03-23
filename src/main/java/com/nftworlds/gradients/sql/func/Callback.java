package com.nftworlds.gradients.sql.func;

public interface Callback<V> {

    void done(V value) throws Exception;

}
