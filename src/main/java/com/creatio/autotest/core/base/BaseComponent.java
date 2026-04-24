package com.creatio.autotest.core.base;

import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.ScrollService;
import com.creatio.autotest.core.services.TypeService;
import com.creatio.autotest.core.services.WaitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseComponent {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    // ══════════════════════════════════════
    // SERVICES — DI через конструктор
    // ══════════════════════════════════════

    protected final ClickService  click;
    protected final WaitService   wait;
    protected final TypeService   type;
    protected final ScrollService scroll;
    protected final Config        config;

    protected BaseComponent() {
        this.click  = new ClickService();
        this.wait   = new WaitService();
        this.type   = new TypeService();
        this.scroll = new ScrollService();
        this.config = Config.getInstance();
    }
}