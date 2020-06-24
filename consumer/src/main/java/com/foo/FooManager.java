package com.foo;

public class FooManager {
  private final Foo foo;

  public FooManager(Foo foo) {
    this.foo = foo;
  }

  public void initialize() {
    foo.mutate();
  }
}
