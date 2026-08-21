package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.testdata.models.CollateralData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCollateralFlow {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    // ══════════════════════════════════════
    // CONTRACT
    // ══════════════════════════════════════

    public abstract void fill(CollateralData data);


}