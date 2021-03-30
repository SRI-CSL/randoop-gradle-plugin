package com.sri.gradle.randoop.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;

public abstract class PluginExtendedTask extends DescribedTask {
    @Input
    public abstract Property<String> getFlakyTestBehavior();

    @Input
    public abstract Property<String> getJunitPackageName() ;

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Integer> getTimeoutSeconds();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Integer> getOutputLimit();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getUsethreads();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getNoErrorRevealingTests();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getJunitReflectionAllowed();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getStopOnErrorTest();


    @OutputDirectory
    public abstract DirectoryProperty getJunitOutputDir();
}
