package com.adyen.core.internals;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.NoFieldShadowingRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenPOJO unit test.
 */

public class InternalsUnitTest {

    @Test
    public void testPojoStructureAndBehavior() {
        final Validator validator = ValidatorBuilder.create()
                // Add Rules to validate structure for POJO_PACKAGE
                // See com.openpojo.validation.rule.impl for more ...
//                .with(new GetterMustExistRule())
//                .with(new SetterMustExistRule())
                .with(new NoFieldShadowingRule())

                // Add Testers to validate behaviour for POJO_PACKAGE
                // See com.openpojo.validation.test.impl for more ...
                .with(new SetterTester())
                .with(new GetterTester())
                .build();


        final List<PojoClass> pojoClasses = new ArrayList<>();
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*HttpClient\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*PaymentTrigger\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*TokenHttpClient\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*ModularPaymentMethods\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*ModuleAvailabilityUtil\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*PaymentMethodServiceFactory\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.internals",
                new FilterClassName("\\w*InputFieldsServiceFactory\\w*$")));


        validator.validate(pojoClasses);
    }
}
