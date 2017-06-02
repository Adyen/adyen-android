package com.adyen.core.utils;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenPOJO unit test.
 */

public class UtilsUnitTest {

    @Test
    public void testPojoStructureAndBehavior() {
        Validator validator = ValidatorBuilder.create()
                // Add Rules to validate structure for POJO_PACKAGE
                // See com.openpojo.validation.rule.impl for more ...
//                .with(new GetterMustExistRule())
//                .with(new SetterMustExistRule())
                // Add Testers to validate behaviour for POJO_PACKAGE
                // See com.openpojo.validation.test.impl for more ...
                .with(new SetterTester())
                .with(new GetterTester())
                .build();

        final List<PojoClass> pojoClasses = new ArrayList<>();
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.utils",
                new FilterClassName("\\w*AdyenRedirectHandlerActivity\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.utils",
                new FilterClassName("\\w*AmountUtil\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.utils",
                new FilterClassName("\\w*AsyncHttpClient\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.utils",
                new FilterClassName("\\w*AsyncImageDownloader\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.utils",
                new FilterClassName("\\w*PaymentResponse\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.utils",
                new FilterClassName("\\w*ValidatorUtil\\w*$")));


        validator.validate(pojoClasses);
    }

}
