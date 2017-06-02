package com.adyen.core.models;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * OpenPOJO unit test.
 */

public class ModelsUnitTest {

    @Ignore @Test
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
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.models",
                new FilterClassName("\\w*Issuer\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.models",
                new FilterClassName("\\w*PaymentRequestResult\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.models",
                new FilterClassName("\\w*PaymentMethod\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.models",
                new FilterClassName("\\w*Amount\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.models",
                new FilterClassName("\\w*Payment\\w*$")));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("com.adyen.core.models",
                new FilterClassName("\\w*PaymentModule\\w*$")));
        validator.validate(pojoClasses);

    }
}
