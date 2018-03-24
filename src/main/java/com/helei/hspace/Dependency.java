package com.helei.hspace;

import com.helei.hspace.ioc.Container;

/**
 * wiring Object dependencies
 * pure java configuration
 */
class Dependency
{
    public static void wire()  {
        Container c = Container.get();
    }
}