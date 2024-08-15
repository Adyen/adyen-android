package com.adyen.checkout.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

internal class TextInLayoutXmlTest {

    @Test
    fun whenAndroidTextIsUsedDirectly_thenIssueIsDetected() {
        lint()
            .files(
                xml(
                    "/res/layout/some_view.xml",
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical">
                        
                        <TextView
                            android:id="@+id/test"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="test" />
                            
                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/textInputLayout"
                            style="@style/AdyenCheckout.TextInputLayout"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content">
                            
                            <com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
                                android:id="@+id/editText"
                                android:hint="hint" />
                        </com.google.android.material.textfield.TextInputLayout>
                    
                    </LinearLayout>
                    """.trimIndent(),
                ),
            )
            .issues(TEXT_IN_LAYOUT_XML_ISSUE)
            .allowMissingSdk()
            .run()
            .expect(
                """
                res/layout/some_view.xml:12: Error: Text should be defined in a style [TextInLayoutXml]
                        android:text="test" />
                        ~~~~~~~~~~~~~~~~~~~
                res/layout/some_view.xml:22: Error: Text should be defined in a style [TextInLayoutXml]
                            android:hint="hint" />
                            ~~~~~~~~~~~~~~~~~~~
                2 errors, 0 warnings
                """,
            )
    }

    @Test
    fun whenAndroidTextIsNotUsed_thenIssueIsNotDetected() {
        lint()
            .files(
                xml(
                    "/res/layout/some_view.xml",
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical">
                        
                        <TextView
                            android:id="@+id/test"
                            style="@styles/some_style"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:textSize="14sp" />
                    
                    </LinearLayout>
                    """.trimIndent(),
                ),
            )
            .issues(TEXT_IN_LAYOUT_XML_ISSUE)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun whenAndroidTextIsUsedInTools_thenIssueIsNotDetected() {
        lint()
            .files(
                xml(
                    "/res/layout/some_view.xml",
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:tools="http://schemas.android.com/tools"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical">
                        
                        <TextView
                            android:id="@+id/test"
                            style="@styles/some_style"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            tools:text="test" />
                    
                    </LinearLayout>
                    """.trimIndent(),
                ),
            )
            .issues(TEXT_IN_LAYOUT_XML_ISSUE)
            .allowMissingSdk()
            .run()
            .expectClean()
    }
}
