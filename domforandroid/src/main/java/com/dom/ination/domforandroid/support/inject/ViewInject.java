package com.dom.ination.domforandroid.support.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by huoxiaobo on 16/8/22.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewInject {
    public int id() default 0;
    public String idStr() default "";
    public String click() default "";
    public String longClick() default "";
    public String itemClick() default "";
    public String itemLongClick() default "";
    public Select select() default @Select(selected = "");
}
