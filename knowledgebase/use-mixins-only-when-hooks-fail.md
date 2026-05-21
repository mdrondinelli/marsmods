# Use Mixins Only When Hooks Fail

Use this when deciding whether to alter vanilla behavior directly.

## Rule

Prefer NeoForge events, registries, capabilities, attachments, datapacks, and tags first.

Use mixins only when:

- vanilla behavior genuinely must change
- no event or hook exists
- datapacks/tags/configs cannot express the behavior

## If A Mixin Is Necessary

- keep injection narrow
- isolate it clearly
- document intent and target behavior
- avoid local-variable fragility
- verify after Minecraft/NeoForge updates

## Reliability

### Make missing targets crash at startup

Without `require`/`allow`, a missing target silently no-ops at runtime — the game loads, the feature just doesn't work. Use these on any critical `@WrapOperation`:

```java
@WrapOperation(
    method = "someMethod(...)V",
    at = @At(value = "INVOKE", target = "..."),
    require = 1, allow = 1
)
```

### Mixin failures are runtime, not compile-time

`./gradlew compileJava` passes even if your `@At` target doesn't exist. Failures only appear in server/client logs as injection errors. Always launch the game to verify, not just compile.

### @WrapMethod only wraps declared methods

`@WrapMethod` cannot wrap inherited methods. If the target class does not override the method (only inherits it), `@WrapMethod` fails at runtime. Check MC source to confirm the method is declared in the exact target class.

### Unannotated method injection

To inject an override into a class that doesn't declare a method, add the method with **no Mixin annotation**. Mixin injects it as a new declared override in bytecode. The Mixin class must extend the parent for this to compile:

```java
@Mixin(SomeSubclass.class)   // subclass doesn't declare someMethod
abstract class MyMixin extends ParentClass {
    // No annotation — Mixin injects this as a new override
    public boolean someMethod(KeyEvent event) {
        // custom logic
        return super.someMethod(event);
    }
}
```

Use this when a subclass stopped overriding a parent method in a newer MC version and `@WrapMethod` therefore fails.

## What Not To Do

- Do not use broad invasive injections for convenience.
- Do not use fragile local captures unless unavoidable.
- Do not stack overlapping redirects.
- Do not use mixins when an event like `BreakSpeed`, `HarvestCheck`, `BlockDropsEvent`, or `ModifyRecipeJsonsEvent` solves the task.
