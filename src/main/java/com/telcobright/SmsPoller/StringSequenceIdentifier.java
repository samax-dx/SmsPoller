package com.telcobright.SmsPoller;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;

public class StringSequenceIdentifier extends org.hibernate.id.IdentityGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor s, Object obj) {
        return super.generate(s, obj).toString();
    }
}
